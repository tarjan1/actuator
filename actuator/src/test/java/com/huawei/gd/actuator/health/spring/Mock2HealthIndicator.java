package com.huawei.gd.actuator.health.spring;

import com.huawei.gd.actuator.health.core.AbstractHealthIndicator;
import com.huawei.gd.actuator.health.core.Health;

public class Mock2HealthIndicator extends AbstractHealthIndicator {
    private final String status;
    private final String message;

    public Mock2HealthIndicator(String status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.status(status).withDetail("message", message);
    }

    @Override
    public String getName() {
        return "mock2";
    }
}
