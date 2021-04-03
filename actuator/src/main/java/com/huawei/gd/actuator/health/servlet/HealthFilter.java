package com.huawei.gd.actuator.health.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import com.huawei.gd.actuator.health.core.CompositeHealth;
import com.huawei.gd.actuator.health.core.HealthIndicator;
import com.huawei.gd.actuator.util.RespHandler;

/**
 * web.xml:
 * 
 * <pre class="code">
 * {@code
 *  <filter>
 *      <servlet-name>healthFilter</servlet-name>
 *      <servlet-class>com.huawei.gd.actuator.health.servlet.HealthFilter</servlet-class>
 *  </filter>
 *  <filter-mapping>
 *      <servlet-name>healthFilter</servlet-name>
 *      <url-pattern>/actuator/health</url-pattern>
 *  </filter-mapping>
 * }
 * </pre>
 * <p>
 * actuator.xml:
 * 
 * <pre class="code">
 * {@code
 *  <bean id="healthFilter" class="com.huawei.gd.actuator.health.spring.HealthFilter">
 *      <property name="healthIndicators" ref="healthIndicators"/>
 *  </bean>
 * }
 * </pre>
 */
public class HealthFilter implements Filter {
    private List<HealthIndicator> healthIndicators;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        CompositeHealth health = RespHandler.merge(healthIndicators);
        String responseStr = RespHandler.chooseObjectToStr(health);
        HttpServletResponse httrvletResponse = (HttpServletResponse) servletResponse;
        RespHandler.responseStrFlush(responseStr, httrvletResponse);
        filterChain.doFilter(servletRequest, httrvletResponse);
        return;
    }

    @Override
    public void destroy() {
    }

    public void setHealthIndicators(List<HealthIndicator> healthIndicators) {
        this.healthIndicators = healthIndicators;
    }
}
