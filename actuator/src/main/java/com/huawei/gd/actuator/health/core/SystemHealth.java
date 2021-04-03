package com.huawei.gd.actuator.health.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link HealthComponent} that represents the overall system health and the available groups.
 *
 */
public final class SystemHealth extends CompositeHealth {

    private final Set<String> groups;

    SystemHealth(String status, Map<String, HealthComponent> instances, Set<String> groups) {
        super(status, instances);
        this.groups = (groups != null) ? new TreeSet(groups) : null;
    }

    public Set<String> getGroups() {
        return this.groups;
    }

}
