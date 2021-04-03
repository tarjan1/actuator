package com.huawei.gd.actuator.health.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base {@link HealthIndicator} implementations that encapsulates creation of {@link Health} instance and error
 * handling.
 * <p>
 * This implementation is only suitable if an {@link Exception} raised from {@link #doHealthCheck(Health.Builder)}
 * should create a {@link Health#DOWN} health status.
 *
 */
public abstract class AbstractHealthIndicator implements HealthIndicator {

    private static final String DEFAULT_MESSAGE = "Health check failed";

    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public final Health health() {
        Health.Builder builder = new Health.Builder();
        try {
            doHealthCheck(builder);
        } catch (Exception ex) {
            if (this.logger.isWarnEnabled()) {
                String message = ex.getMessage();
                this.logger.warn(message.isEmpty() ? DEFAULT_MESSAGE : message, ex);
            }
            builder.down(ex);
        }
        return builder.build();
    }

    /**
     * Actual health check logic.
     * 
     * @param builder the {@link Health.Builder} to report health status and details
     * @throws Exception any {@link Exception} that should create a {@link Health#DOWN} system status.
     */
    protected abstract void doHealthCheck(Health.Builder builder) throws Exception;

}
