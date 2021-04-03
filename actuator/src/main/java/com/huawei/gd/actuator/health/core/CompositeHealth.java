package com.huawei.gd.actuator.health.core;

import java.util.Map;
import java.util.TreeMap;

import com.huawei.gd.actuator.util.Assert;

/**
 * A {@link HealthComponent} that is composed of other {@link HealthComponent} instances. Used to provide a unified view
 * of related components. For example, a database health indicator may be a composite containing the {@link Health} of
 * each datasource connection.
 */
public class CompositeHealth extends HealthComponent {

    private final String status;

    private final Map<String, HealthComponent> components;

    public CompositeHealth(String status, Map<String, HealthComponent> components) {
        Assert.notNull(status, "Status must not be null");
        this.status = status;
        this.components = sort(components);

    }

    private Map<String, HealthComponent> sort(Map<String, HealthComponent> components) {
        return (components != null) ? new TreeMap(components) : components;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    public Map<String, HealthComponent> getComponents() {
        return this.components;
    }
}
