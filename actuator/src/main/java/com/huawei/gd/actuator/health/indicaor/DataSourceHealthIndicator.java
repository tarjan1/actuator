package com.huawei.gd.actuator.health.indicaor;

import com.huawei.gd.actuator.health.core.AbstractHealthIndicator;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import com.huawei.gd.actuator.util.ObjectUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * {@link HealthIndicator} that tests the status and optionally runs a test query.
 * <p>
 * 支持两种配置方式：
 * 1.支持单个datasource，不需要提供名字区分.如果datasource是一个AbstractRoutingDataSource，则进行遍历处理。
 * 2.支持多个datasource，需要提供名字区分(通过map注入)
 */
public class DataSourceHealthIndicator extends AbstractHealthIndicator implements DisposableBean {
    public static final String NAME = "db";
    public static final String DATABASE_UNKNOWN = "unknown database";
    public static final String DATABASE_VERSION_UNKNOWN = "unknown database-version";
    private static final String DEFAULT_QUERY = "SELECT 1 from DUAL";
    private static final long DEFAULT_TIME_LIMIT = 3000L;
    private ExecutorService healthExecutor;
    private DataSource dataSource;
    private Map<Object, ? extends CommonDataSource> dataSources;
    private String checkSql = DEFAULT_QUERY;
    private long timeLimit = DEFAULT_TIME_LIMIT;

    /**
     * Create a new {@link DataSourceHealthIndicator} instance.
     */
    public DataSourceHealthIndicator(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
        initExecutor();
    }

    /**
     * Create a new {@link DataSourceHealthIndicator} instance.
     */
    public DataSourceHealthIndicator(Map<Object, ? extends CommonDataSource> dataSources) {
        super();
        this.dataSources = dataSources;
        initExecutor();
    }

    private void initExecutor() {
        if (dataSource != null) {
            // 针对AbstractRoutingDataSource进行特殊处理,如果存在多个则进行替换
            if (ObjectUtils.isExist("org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource")) {
                if (dataSource instanceof AbstractRoutingDataSource) {
                    try {
                        Field field = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
                        field.setAccessible(true);
                        Map<Object, DataSource> resolvedDataSources = (Map<Object, DataSource>) field.get(dataSource);
                        if (resolvedDataSources != null) {
                            dataSource = null;
                            dataSources = resolvedDataSources;
                        }
                    } catch (Exception e) {
                        ignore(e);
                    }
                }
            }
        }

        int dataSourceSize = 1;
        if (dataSources != null) {
            dataSourceSize = dataSources.size();
        }

        // 根据数据源的数量来决定线程池的配置，约束线程数最大为4，队列长度为数据源个数*2
        int capacity = Math.min(dataSourceSize, 4);
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(dataSourceSize * 2);
        this.healthExecutor = new ThreadPoolExecutor(1, capacity, 60L, TimeUnit.SECONDS, queue);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // 两个必须要设置一个
        if (this.dataSource == null && this.dataSources == null) {
            builder.up().withDetail("database", "unknown");
        } else {
            doDataSourceHealthCheck(builder);
        }
    }

    private void doDataSourceHealthCheck(final Health.Builder builder) throws Exception {
        // 针对单个数据源
        if (dataSource != null) {
            Health health = null;
            try {
                // 针对数据库异常这种情况，维护建议采用避免让探针挂死导致重启，是否重启由维护进行控制
                Future<Health> future = healthExecutor.submit(new DataSourceCheckRunner(dataSource));
                health = future.get(timeLimit, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                // 执行报错，可能是超时，或者被阻塞，设置为未知状态
                builder.unknown().withException(ex);
                return;
            }
            if (health != null) {
                builder.status(health.getStatus()).withDetails(health.getDetails());
            }
            return;
        }

        // 针对多个数据源
        Map<String, Future<Health>> healths = new LinkedHashMap<String, Future<Health>>();
        if (dataSources != null) {
            Iterator<? extends Map.Entry<Object, ? extends CommonDataSource>> it = dataSources.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, ? extends CommonDataSource> en = it.next();
                String key = en.getKey().toString();
                try {
                    Future<Health> future = healthExecutor.submit(new DataSourceCheckRunner(en.getValue()));
                    healths.put(key, future);
                } catch (Exception e) {
                    // 这里可能除非reject handler导致报错，设置为未知状态，不统计入全部数据库的健康状态
                    Health.Builder headlthBuilder = new Health.Builder().unknown().withException(e);
                    builder.withDetail(key, headlthBuilder.build());
                }
            }
        }
        boolean isAllUp = true;
        Iterator<Map.Entry<String, Future<Health>>> it = healths.entrySet().iterator();
        long start = System.currentTimeMillis();
        boolean skip = false;
        while (it.hasNext()) {
            Map.Entry<String, Future<Health>> en = it.next();
            Future<Health> future = en.getValue();
            if (skip) {
                Health.Builder headlthBuilder = new Health.Builder().unknown().withDetail("error", "skip health check");
                builder.withDetail(en.getKey(), headlthBuilder.build());
                continue;
            }

            Health health;
            try {
                // 针对数据库异常这种情况，维护建议采用避免让探针挂死导致重启，是否重启由维护进行控制
                // 这里catch一下异常，发现异常说明当前检查失败,如果从Future中取值超时则判断为失败的，避免数据库异常导致连接挂起
                health = future.get(timeLimit, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                // 执行报错，可能是超时，或者被阻塞，设置为未知状态，不统计入全部数据库的健康状态
                Health.Builder headlthBuilder = new Health.Builder().unknown().withException(ex);
                builder.withDetail(en.getKey(), headlthBuilder.build());
                continue;
            }
            if (health != null) {
                if (!Health.UP.equals(health.getStatus())) {
                    isAllUp = false;
                }
                builder.withDetail(en.getKey(), health);
            }
            // 总耗时限制
            if (System.currentTimeMillis() - start > timeLimit) {
                skip = true;
            }
        }
        // 所有数据源检查都正常才设置为UP状态
        if (isAllUp) {
            builder.up();
        } else {
            builder.down();
        }
    }

    /**
     * 通过查询一个简单SQL检查数据库是否正常
     *
     * @param connection
     * @param builder
     */
    private void checkDataBaseState(Connection connection, Health.Builder builder) {
        if (connection == null || builder == null) {
            return;
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(checkSql);
            rs.next();
            builder.withDetail("result", rs.getString(1));
            builder.up();
        } catch (SQLException ex) {
            builder.down().withException(ex);
        } finally {
            closeConnectionResources(rs, st, connection);
            builder.withDetail("validationQuery", checkSql);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void ignore(Exception e) {
        // ignore exception
    }

    private void closeConnectionResources(ResultSet rs, Statement st, Connection connection) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                ignore(ex);
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ex) {
                ignore(ex);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                ignore(ex);
            }
        }
    }

    @Override
    public void destroy() {
        healthExecutor.shutdownNow();
    }

    public void setCheckSql(String checkSql) {
        this.checkSql = checkSql;
    }

    public void setTimeLimit(long timeLimit) {
        if (timeLimit <= 0) {
            throw new IllegalArgumentException("timeLimit must larger than 0");
        }
        this.timeLimit = timeLimit;
    }

    // 采用default修饰，仅仅用于测试
    void setTestExecutor(ExecutorService healthExecutor) {
        this.healthExecutor = healthExecutor;
    }

    private class DataSourceCheckRunner implements Callable {
        private CommonDataSource dataSource;

        DataSourceCheckRunner(CommonDataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Health call() {
            Connection connection;
            Health.Builder healthBuilder = new Health.Builder();
            try {
                if (dataSource instanceof DataSource) {
                    connection = ((DataSource) dataSource).getConnection();
                } else if (dataSource instanceof XADataSource) {
                    connection = ((XADataSource) dataSource).getXAConnection().getConnection();
                } else {
                    throw new SQLException("unsupport datasource type: " + dataSource.getClass().getName());
                }
                checkDataBaseState(connection, healthBuilder);
            } catch (SQLException ex) {
                healthBuilder.down().withException(ex);
            }
            return healthBuilder.build();
        }
    }
}
