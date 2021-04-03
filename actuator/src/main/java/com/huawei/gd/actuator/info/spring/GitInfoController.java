package com.huawei.gd.actuator.info.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huawei.gd.actuator.info.GitInfo;

/**
 * 定义接口返回git状态信息
 */
@Controller
public class GitInfoController {
    @RequestMapping(value = "/actuator/info", produces = "application/json;charset=UTF-8", method = RequestMethod.GET)
    @ResponseBody
    public GitInfo getGitInfo() {
        return GitInfo.loadInfo();
    }
}
