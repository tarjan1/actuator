package com.huawei.gd.actuator.health.indicaor;

import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DataSourceHealthIndicatorTest {
    private long timeLimit = 200L;
    private JdbcDataSource ds1;
    private JdbcDataSource ds2;
    private JdbcDataSource ds3;
    private JdbcDataSource ds4;

    @Before
    public void setup() {
        // 正常
        ds1 = new JdbcDataSource();
        ds1.setURL("jdbc:h2:mem:");
        ds1.setUser("");
        ds1.setPassword("");

        // 异常(完全不可用)
        ds2 = new JdbcDataSource();
        ds2.setURL("jdbc:h2:error:");
        ds2.setUser("");
        ds2.setPassword("");

        // 异常(返回超时)
        ds3 = new JdbcDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                try {
                    TimeUnit.MILLISECONDS.sleep(timeLimit + 5);
                } catch (InterruptedException e) {
                    // ignore
                }
                throw new SQLException("not enough connection");
            }
        };
        ds3.setURL("jdbc:h2:mem:");
        ds3.setUser("");
        ds3.setPassword("");

        // 异常(较快返回，但多次会超时)
        ds4 = new JdbcDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                try {
                    TimeUnit.MILLISECONDS.sleep(timeLimit * 3 / 4);
                } catch (InterruptedException e) {
                    // ignore
                }
                throw new SQLException("not enough connection");
            }
        };
        ds4.setURL("jdbc:h2:mem:");
        ds4.setUser("");
        ds4.setPassword("");
    }

    @Test
    public void testGoodDatasource() {
        HealthIndicator healthIndicator = new DataSourceHealthIndicator(ds1);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails().get("result")).isEqualTo("1");
    }

    @Test
    public void testBadDatasource() {
        HealthIndicator healthIndicator = new DataSourceHealthIndicator(ds2);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat((String) health.getDetails().get("error")).startsWith("org.h2.jdbc.JdbcSQLNonTransientConnectionException: A file path that is implicitly relative");
    }

    @Test
    public void testTimeoutDatasource() {
        DataSourceHealthIndicator healthIndicator = new DataSourceHealthIndicator(ds3);
        healthIndicator.setTimeLimit(timeLimit);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UNKNOWN);
        assertThat((String) health.getDetails().get("error")).startsWith("java.util.concurrent.TimeoutException");
    }

    @Test
    public void testNearTimeoutDatasource() {
        DataSourceHealthIndicator healthIndicator = new DataSourceHealthIndicator(ds4);
        healthIndicator.setTimeLimit(timeLimit);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat((String) health.getDetails().get("error")).isEqualTo("java.sql.SQLException: not enough connection");
    }

    @Test
    public void testDatasourceIsNull() {
        HealthIndicator healthIndicator = new DataSourceHealthIndicator((DataSource) null);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails().get("database")).isEqualTo("unknown");
    }

    @Test
    public void testDatasourceName() {
        HealthIndicator healthIndicator = new DataSourceHealthIndicator(ds1);
        assertThat(healthIndicator.getName()).isEqualTo(DataSourceHealthIndicator.NAME);
    }

    @Test
    public void testMultiDatasource() {
        Map<Object, DataSource> ds = new HashMap<Object, DataSource>();
        for (int i = 0; i < 20; i++) {
            ds.put(String.format("dataSource_2%02d", i), ds1);
        }
        HealthIndicator healthIndicator = new DataSourceHealthIndicator(ds);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        Iterator<Map.Entry<Object, DataSource>> it = ds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, DataSource> en = it.next();
            Health tmp = (Health) health.getDetails().get(en.getKey());
            assertThat(tmp.getStatus()).isEqualTo(Health.UP);
            Map<String, Object> details = tmp.getDetails();
            assertThat(details.get("result")).isEqualTo("1");
        }
    }

    @Test
    public void testMultiTimeoutDatasource() {
        Map<Object, DataSource> ds = new LinkedHashMap<Object, DataSource>();
        ds.put("dataSource_1", ds1);
        ds.put("dataSource_2", ds4);
        ds.put("dataSource_3", ds4);
        ds.put("dataSource_4", ds4);
        DataSourceHealthIndicator healthIndicator = new DataSourceHealthIndicator(ds);
        healthIndicator.setTestExecutor(Executors.newSingleThreadExecutor()); // 仅仅用于测试
        healthIndicator.setTimeLimit(timeLimit);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        Health h1 = (Health) health.getDetails().get("dataSource_1");
        assertThat(h1.getStatus()).isEqualTo(Health.UP); // 立即反馈，所以飞快
        Health h2 = (Health) health.getDetails().get("dataSource_2");
        assertThat(h2.getStatus()).isEqualTo(Health.DOWN); // 返回但是超时
        assertThat((String) h2.getDetails().get("error")).isEqualTo("java.sql.SQLException: not enough connection");
        Health h3 = (Health) health.getDetails().get("dataSource_3");
        assertThat(h3.getStatus()).isEqualTo(Health.DOWN); // 返回但是超时，并且总时间也超时
        assertThat((String) h3.getDetails().get("error")).startsWith("java.sql.SQLException: not enough connection");
        Health h4 = (Health) health.getDetails().get("dataSource_4");
        assertThat(h4.getStatus()).isEqualTo(Health.UNKNOWN); // 因为总时间超时，所以没有执行
        assertThat((String) h4.getDetails().get("error")).startsWith("skip health check");
    }

    @Test
    public void testRoutingDataSource() {
        AbstractRoutingDataSource ds = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return null;
            }
        };
        Map<Object, Object> inter = new HashMap<Object, Object>();
        for (int i = 0; i < 2; i++) {
            inter.put(String.format("dataSource_%d", i), ds1);
        }
        ds.setTargetDataSources(inter);
        ds.afterPropertiesSet();
        DataSourceHealthIndicator healthIndicator = new DataSourceHealthIndicator(ds);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        Health h1 = (Health) health.getDetails().get("dataSource_0");
        assertThat(h1.getStatus()).isEqualTo(Health.UP);
        Health h2 = (Health) health.getDetails().get("dataSource_1");
        assertThat(h2.getStatus()).isEqualTo(Health.UP);
    }
}
