package com.huawei.gd.actuator.info.servlet;

import com.huawei.gd.actuator.info.GitInfo;
import com.huawei.gd.actuator.util.RespHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * web.xml:
 *
 * <pre class="code">
 * {@code
 *  <filter>
 *      <servlet-name>gitInfoFilter</servlet-name>
 *      <servlet-class>com.huawei.gd.actuator.info.servlet.GitInfoFilter</servlet-class>
 *  </filter>
 *  <filter-mapping>
 *      <servlet-name>gitInfoFilter</servlet-name>
 *      <url-pattern>/actuator/info</url-pattern>
 *  </filter-mapping>
 * }
 * </pre>
 */
public class GitInfoFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException {
        GitInfo gitInfo = GitInfo.loadInfo();
        String responseStr = RespHandler.chooseObjectToStr(gitInfo);
        HttpServletResponse httrvletResponse = (HttpServletResponse) servletResponse;
        RespHandler.responseStrFlush(responseStr, httrvletResponse);
        return;
    }

    @Override
    public void destroy() {
    }
}
