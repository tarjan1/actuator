package com.huawei.gd.actuator.health.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link SystemHealth}.
 */
public class SystemHealthTests {

    @Test
    public void serializeWithJacksonReturnsValidJson() throws Exception {
        Map<String, HealthComponent> components = new LinkedHashMap();
        components.put("db1", Health.up().build());
        components.put("db2", Health.down().withDetail("a", "b").build());
        Set<String> groups = new LinkedHashSet(Arrays.asList("liveness", "readiness"));
        CompositeHealth health = new SystemHealth(Health.UP, components, groups);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(health);
        assertThat(json).isEqualTo("{\"status\":\"UP\",\"components\":" + "{\"db1\":{\"status\":\"UP\",\"details\":{}},"
                + "\"db2\":{\"status\":\"DOWN\",\"details\":{\"a\":\"b\"}}},\"groups\":[\"liveness\",\"readiness\"]}");
    }

}
