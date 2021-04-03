package com.huawei.gd.actuator.health.spring.indicaor;

import java.util.Properties;

import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.util.Assert;

import com.huawei.gd.actuator.health.core.AbstractHealthIndicator;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;

/**
 * Simple implementation of a {@link HealthIndicator} returning status information for Redis data stores.
 *
 */
public class RedisHealthIndicator extends AbstractHealthIndicator {
    public static final String NAME = "redis";

    static final String VERSION = "version";

    static final String REDIS_VERSION = "redis_version";

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        this.redisConnectionFactory = connectionFactory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
        try {
            if (connection instanceof RedisClusterConnection) {
                ClusterInfo clusterInfo = ((RedisClusterConnection) connection).clusterGetClusterInfo();
                builder.up().withDetail("cluster_size", clusterInfo.getClusterSize())
                        .withDetail("slots_up", clusterInfo.getSlotsOk())
                        .withDetail("slots_fail", clusterInfo.getSlotsFail());
            } else {
                Properties info = connection.info();
                builder.up().withDetail(VERSION, info.getProperty(REDIS_VERSION));
            }
        } finally {
            RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
