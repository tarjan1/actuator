package com.huawei.gd.actuator.info.spring;

import com.huawei.gd.actuator.info.GitInfo;
import com.huawei.gd.actuator.util.RespHandler;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * web.xml:
 *
 * <pre class="code">
 * {@code
 *  <servlet>
 *      <servlet-name>gitInfoRequestServlet</servlet-name>
 *      <servlet-class>com.huawei.gd.actuator.info.spring.GitInfoRequestHandler</servlet-class>
 *  </servlet>
 *  <servlet-mapping>
 *      <servlet-name>gitInfoRequestServlet</servlet-name>
 *      <url-pattern>/actuator/info</url-pattern>
 *  </servlet-mapping>
 * }
 * </pre>
 *
 * </pre>
 */
public class GitInfoRequestHandler implements HttpRequestHandler {
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        GitInfo gitInfo = GitInfo.loadInfo();
        String responseStr = RespHandler.chooseObjectToStr(gitInfo);
        RespHandler.responseStrFlush(responseStr, response);
        return;
    }
}
