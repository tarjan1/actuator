package com.huawei.gd.actuator.health.core;

import com.huawei.gd.actuator.health.spring.HealthController;

/**
 * Strategy interface used to contribute {@link Health} to the results returned from the {@link HealthController}.
 * 
 */
public interface HealthIndicator extends HealthContributor {

    /**
     * Return an indication of health.
     * 
     * @return the health
     */
    Health health();

    /**
     * Return a component of name.
     *
     * @return String
     */
    String getName();

}
