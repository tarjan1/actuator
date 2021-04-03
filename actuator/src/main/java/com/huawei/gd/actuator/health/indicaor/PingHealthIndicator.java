package com.huawei.gd.actuator.health.indicaor;

import com.huawei.gd.actuator.health.core.AbstractHealthIndicator;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthIndicator;

/**
 * Default implementation of {@link HealthIndicator} that returns {@link Health#UP}.
 *
 */
public class PingHealthIndicator extends AbstractHealthIndicator {
    public static final String NAME = "ping";

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
