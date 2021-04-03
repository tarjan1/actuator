package com.huawei.gd.actuator.health.core;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for {@link Health}.
 */
public class HealthTests {

    @Test
    public void statusMustNotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                new Health.Builder(null, null);
            }
        }).withMessageContaining("Status must not be null");
    }

    @Test
    public void createWithStatus() {
        Health health = Health.status(Health.UP).build();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void createWithDetails() {
        Health health = new Health.Builder(Health.UP, Collections.singletonMap("a", "b")).build();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails()).containsOnly(entry("a", "b"));
    }

    @Test
    public void equalsAndHashCode() {
        Health h1 = new Health.Builder(Health.UP, Collections.singletonMap("a", "b")).build();
        Health h2 = new Health.Builder(Health.UP, Collections.singletonMap("a", "b")).build();
        Health h3 = new Health.Builder(Health.UP).build();
        assertThat(h1).isEqualTo(h1);
        assertThat(h1).isEqualTo(h2);
        assertThat(h1).isNotEqualTo(h3);
        assertThat(h1.hashCode()).isEqualTo(h1.hashCode());
        assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        assertThat(h1.hashCode()).isNotEqualTo(h3.hashCode());
    }

    @Test
    public void withException() {
        RuntimeException ex = new RuntimeException("bang");
        Health health = new Health.Builder(Health.UP, Collections.singletonMap("a", "b")).withException(ex).build();
        assertThat(health.getDetails()).containsOnly(entry("a", "b"),
                entry("error", "java.lang.RuntimeException: bang"));
    }

    @Test
    public void withDetails() {
        Health health = new Health.Builder(Health.UP, Collections.singletonMap("a", "b")).withDetail("c", "d").build();
        assertThat(health.getDetails()).containsOnly(entry("a", "b"), entry("c", "d"));
    }

    @Test
    public void withDetailsMap() {
        Map<String, Object> details = new LinkedHashMap();
        details.put("a", "b");
        details.put("c", "d");
        Health health = Health.up().withDetails(details).build();
        assertThat(health.getDetails()).containsOnly(entry("a", "b"), entry("c", "d"));
    }

    @Test
    public void withDetailsMapDuplicateKeys() {
        Map<String, Object> details = new LinkedHashMap();
        details.put("c", "d");
        details.put("a", "e");
        Health health = Health.up().withDetail("a", "b").withDetails(details).build();
        assertThat(health.getDetails()).containsOnly(entry("a", "e"), entry("c", "d"));
    }

    @Test
    public void withDetailsMultipleMaps() {
        Map<String, Object> details1 = new LinkedHashMap();
        details1.put("a", "b");
        details1.put("c", "d");
        Map<String, Object> details2 = new LinkedHashMap();
        details1.put("a", "e");
        details1.put("1", "2");
        Health health = Health.up().withDetails(details1).withDetails(details2).build();
        assertThat(health.getDetails()).containsOnly(entry("a", "e"), entry("c", "d"), entry("1", "2"));
    }

    @Test
    public void unknownWithDetails() {
        Health health = new Health.Builder().unknown().withDetail("a", "b").build();
        assertThat(health.getStatus()).isEqualTo(Health.UNKNOWN);
        assertThat(health.getDetails()).containsOnly(entry("a", "b"));
    }

    @Test
    public void unknown() {
        Health health = new Health.Builder().unknown().build();
        assertThat(health.getStatus()).isEqualTo(Health.UNKNOWN);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void upWithDetails() {
        Health health = new Health.Builder().up().withDetail("a", "b").build();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails()).containsOnly(entry("a", "b"));
    }

    @Test
    public void up() {
        Health health = new Health.Builder().up().build();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void downWithException() {
        RuntimeException ex = new RuntimeException("bang");
        Health health = Health.down(ex).build();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat(health.getDetails()).containsOnly(entry("error", "java.lang.RuntimeException: bang"));
    }

    @Test
    public void down() {
        Health health = Health.down().build();
        assertThat(health.getStatus()).isEqualTo(Health.DOWN);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void outOfService() {
        Health health = Health.outOfService().build();
        assertThat(health.getStatus()).isEqualTo(Health.OUT_OF_SERVICE);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void status() {
        Health health = Health.status(Health.UP).build();
        assertThat(health.getStatus()).isEqualTo(Health.UP);
        assertThat(health.getDetails()).isEmpty();
    }

    @Test
    public void serializeWithJacksonReturnsValidJson() throws Exception {
        Health health = Health.down().withDetail("a", "b").build();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(health);
        assertThat(json).isEqualTo("{\"status\":\"DOWN\",\"details\":{\"a\":\"b\"}}");
    }

}
