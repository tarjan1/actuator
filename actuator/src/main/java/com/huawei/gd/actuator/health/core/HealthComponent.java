package com.huawei.gd.actuator.health.core;

/**
 * An component that contributes data to results
 */
public abstract class HealthComponent {
    HealthComponent() {
    }

    /**
     * Return the status of the component.
     * 
     * @return the component status
     */
    public abstract String getStatus();
}
