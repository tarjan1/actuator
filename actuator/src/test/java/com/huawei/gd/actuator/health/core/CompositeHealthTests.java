package com.huawei.gd.actuator.health.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test for {@link CompositeHealth}.
 *
 */
public class CompositeHealthTests {

    @Test
    public void createWhenStatusIsNullThrowsException() {
        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                new CompositeHealth(null, Collections.<String, HealthComponent> emptyMap());
            }
        }).withMessage("Status must not be null");
    }

    @Test
    public void getStatusReturnsStatus() {
        CompositeHealth health = new CompositeHealth(Health.UP, Collections.<String, HealthComponent> emptyMap());
        assertThat(health.getStatus()).isEqualTo(Health.UP);
    }

    @Test
    public void getComponentReturnsComponents() {
        Map<String, HealthComponent> components = new LinkedHashMap();
        components.put("a", Health.up().build());
        CompositeHealth health = new CompositeHealth(Health.UP, components);
        assertThat(health.getComponents()).isEqualTo(components);
    }

    @Test
    public void serializeWithJacksonReturnsValidJson() throws Exception {
        Map<String, HealthComponent> components = new LinkedHashMap();
        components.put("db1", Health.up().build());
        components.put("db2", Health.down().withDetail("a", "b").build());
        CompositeHealth health = new CompositeHealth(Health.UP, components);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(health);
        assertThat(json).isEqualTo("{\"status\":\"UP\",\"components\":" + "{\"db1\":{\"status\":\"UP\",\"details\":{}},"
                + "\"db2\":{\"status\":\"DOWN\",\"details\":{\"a\":\"b\"}}}}");
    }

}
