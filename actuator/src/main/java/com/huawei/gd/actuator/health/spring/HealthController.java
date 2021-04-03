package com.huawei.gd.actuator.health.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huawei.gd.actuator.health.core.CompositeHealth;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import com.huawei.gd.actuator.util.RespHandler;

/**
 * 定义接口返回服务状态信息
 */
@Controller
public class HealthController {
    @Autowired
    private List<HealthIndicator> healthIndicators;

    @RequestMapping(value = "/actuator/health", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public CompositeHealth getHealth() {
        return RespHandler.merge(healthIndicators);
    }
}
