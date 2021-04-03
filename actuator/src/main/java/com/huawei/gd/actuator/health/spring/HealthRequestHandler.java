package com.huawei.gd.actuator.health.spring;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import com.huawei.gd.actuator.health.core.CompositeHealth;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import com.huawei.gd.actuator.util.RespHandler;

/**
 * web.xml:
 * 
 * <pre class="code">
 * {@code
 *  <servlet>
 *      <servlet-name>healthRequestServlet</servlet-name>
 *      <servlet-class>org.springframework.web.context.support.HttpRequestHandlerServlet</servlet-class>
 *  </servlet>
 *  <servlet-mapping>
 *      <servlet-name>healthRequestServlet</servlet-name>
 *      <url-pattern>/actuator/health</url-pattern>
 *  </servlet-mapping>
 * }
 * </pre>
 *
 * actuator.xml:
 * 
 * <pre class="code">
 * {@code
 *  <bean id="healthRequestServlet" class="com.huawei.gd.actuator.health.spring.HealthRequestHandler">
 *      <property name="healthIndicators" ref="healthIndicators"/>
 *  </bean>
 * }
 * </pre>
 */
public class HealthRequestHandler implements HttpRequestHandler {
    private List<HealthIndicator> healthIndicators;

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        CompositeHealth health = RespHandler.merge(healthIndicators);
        String responseStr = RespHandler.chooseObjectToStr(health);
        RespHandler.responseStrFlush(responseStr, httpServletResponse);
        return;
    }

    public void setHealthIndicators(List<HealthIndicator> healthIndicators) {
        this.healthIndicators = healthIndicators;
    }
}
