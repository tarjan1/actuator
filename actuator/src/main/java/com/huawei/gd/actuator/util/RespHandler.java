package com.huawei.gd.actuator.util;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.gd.actuator.health.core.CompositeHealth;
import com.huawei.gd.actuator.health.core.Health;
import com.huawei.gd.actuator.health.core.HealthComponent;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.gd.actuator.util.ObjectUtils.isExist;

/**
 * 健康检查response 处理类
 */
public class RespHandler {

    /**
     * 拼装每个健康组件
     *
     * @param healthIndicators
     * @return
     */
    public static CompositeHealth merge(List<HealthIndicator> healthIndicators) {
        String outsideStatus = Health.UP;
        Map<String, HealthComponent> components = new LinkedHashMap();
        for (HealthIndicator healthIndicator : healthIndicators) {
            Health health = healthIndicator.health();
            if (!health.getStatus().equals(Health.UP)) {
                outsideStatus = health.getStatus();
            }
            components.put(healthIndicator.getName(), health);
        }
        return new CompositeHealth(outsideStatus, components);
    }

    /**
     * 将健康组合对象 转换为jsonStr（会判断选择jackson || fastjson || gson || json-lib 其中之一做json处理）
     *
     * @param respObj
     * @return
     * @throws IOException
     */
    public static String chooseObjectToStr(Object respObj) throws IOException {
        String responseStr = "";

        if (isExist("com.fasterxml.jackson.databind.ObjectMapper")) {
            ObjectMapper mapper = new ObjectMapper();
            responseStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(respObj);
        } else if (isExist("com.alibaba.fastjson.JSON")) {
            responseStr = JSON.toJSONString(respObj, true);
        } else if (isExist("org.json.JSONObject")) {
            org.json.JSONObject crmJsonObject = new org.json.JSONObject(respObj);
            responseStr = crmJsonObject.toString();
        } else if (isExist("com.google.gson.Gson")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            responseStr = gson.toJson(respObj);
        } else if (isExist("net.sf.json.JSONObject")) {
            JSONObject jsonObject = net.sf.json.JSONObject.fromObject(respObj);
            responseStr = jsonObject.toString(4);
        }
        if (responseStr.equals("")) {
            responseStr = "jackson || fastjson || gson || json-lib ||ngcrmjson not found";
        }
        return responseStr;
    }

    /**
     * 将responseStr写入HttpServletResponse
     *
     * @param responseStr
     * @param response
     * @throws IOException
     */
    public static void responseStrFlush(String responseStr, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        out.print(responseStr);
        out.flush();
    }
}
