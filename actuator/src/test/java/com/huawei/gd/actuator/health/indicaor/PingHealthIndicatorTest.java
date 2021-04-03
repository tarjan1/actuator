package com.huawei.gd.actuator.health.indicaor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.huawei.gd.actuator.health.core.Health;

public class PingHealthIndicatorTest {
    @Test
    public void indicatesUp() {
        PingHealthIndicator healthIndicator = new PingHealthIndicator();
        assertThat(healthIndicator.health().getStatus()).isEqualTo(Health.UP);
    }
}
