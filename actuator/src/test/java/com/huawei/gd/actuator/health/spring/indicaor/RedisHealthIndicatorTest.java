package com.huawei.gd.actuator.health.spring.indicaor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.*;

import com.huawei.gd.actuator.health.core.Health;

/**
 * Tests for {@link RedisHealthIndicator}.
 *
 */
public class RedisHealthIndicatorTest {

    @Test
    public void redisIsUp() {
        Properties info = new Properties();
        info.put("redis_version", "2.8.9");
        RedisConnection redisConnection = mock(RedisConnection.class);
        given(redisConnection.info()).willReturn(info);
        RedisHealthIndicator healthIndicator = createHealthIndicator(redisConnection);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails().get("version")).isEqualTo("2.8.9");
    }

    @Test
    public void redisIsDown() {
        RedisConnection redisConnection = mock(RedisConnection.class);
        given(redisConnection.info()).willThrow(new RedisConnectionFailureException("Connection failed"));
        RedisHealthIndicator healthIndicator = createHealthIndicator(redisConnection);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat((String) health.getDetails().get("error")).contains("Connection failed");
    }

    private RedisHealthIndicator createHealthIndicator(RedisConnection redisConnection) {
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        given(redisConnectionFactory.getConnection()).willReturn(redisConnection);
        return new RedisHealthIndicator(redisConnectionFactory);
    }

    @Test
    public void redisClusterIsUp() {
        Properties clusterProperties = new Properties();
        clusterProperties.setProperty("cluster_size", "4");
        clusterProperties.setProperty("cluster_slots_ok", "4");
        clusterProperties.setProperty("cluster_slots_fail", "0");
        List<RedisClusterNode> redisMasterNodes = Arrays.asList(new RedisClusterNode("127.0.0.1", 7001),
                new RedisClusterNode("127.0.0.2", 7001));
        RedisClusterConnection redisConnection = mock(RedisClusterConnection.class);
        given(redisConnection.clusterGetNodes()).willReturn(redisMasterNodes);
        given(redisConnection.clusterGetClusterInfo()).willReturn(new ClusterInfo(clusterProperties));
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        given(redisConnectionFactory.getConnection()).willReturn(redisConnection);
        RedisHealthIndicator healthIndicator = new RedisHealthIndicator(redisConnectionFactory);
        Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails().get("cluster_size")).isEqualTo(4L);
        assertThat(health.getDetails().get("slots_up")).isEqualTo(4L);
        assertThat(health.getDetails().get("slots_fail")).isEqualTo(0L);
        verify(redisConnectionFactory, atLeastOnce()).getConnection();
    }

}
