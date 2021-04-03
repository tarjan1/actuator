package com.huawei.gd.actuator.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.gd.actuator.health.core.CompositeHealth;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthComponent;

import net.sf.json.JSONObject;

public class RespHandlerTest {
    @Test
    public void ObjectToJsonStr() throws IOException, JSONException {
        Map<String, HealthComponent> components = new LinkedHashMap();
        components.put("db1", Health.up().build());
        components.put("db2", Health.down().withDetail("a", "b").build());
        CompositeHealth health = new CompositeHealth(Health.UP, components);
        String healthStr = "{\"status\":\"UP\",\"components\":" + "{\"db1\":{\"status\":\"UP\",\"details\":{}},"
                + "\"db2\":{\"status\":\"DOWN\",\"details\":{\"a\":\"b\"}}}}";
        // jackson
        ObjectMapper mapper = new ObjectMapper();
        String jacksonStr = mapper.writeValueAsString(health);
        assertThat(jacksonStr).isEqualTo(healthStr);
        // fastjson
        String fastjsonStr = JSON.toJSONString(health, true);
        assertThat(fastjsonStr).contains("\"status\":\"UP\"");
        // gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String gsonStr = gson.toJson(health);
        assertThat(gsonStr).contains("\"status\": \"UP\"");
        // json-lib
        JSONObject jsonObject = JSONObject.fromObject(health);
        String jsonLibStr = jsonObject.toString(4);
        assertThat(jsonLibStr).contains("\"status\": \"UP\"");
        // ngcrm-json
        org.json.JSONObject crmJsonObject = new org.json.JSONObject(health);
        String crmJsonStr = crmJsonObject.toString(4);
        assertThat(crmJsonStr).contains("\"status\": \"UP\"");
    }
}
