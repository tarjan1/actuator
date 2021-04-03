package com.huawei.gd.actuator.info;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Getter
@Setter
public class GitInfo {
    public static final String GIT_PROPERTIES = "git.properties";
    public static final String GIT_PROPERTIES_NOT_EXIST = "UNKNOWN";
    public static final String GIT_BRANCH = "git.branch";
    public static final String GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev";
    public static final String GIT_COMMIT_TIME = "git.commit.time";
    public static final String GIT_BUILD_TIME = "git.build.time";

    private String gitBranch;
    private String gitCommitIdAbbrev;
    private String gitCommitTime;
    private String gitBuildTime;

    public static GitInfo loadInfo() {
        try {
            // 如果不存在git.properties文件，将会抛异常
            Properties props = PropertiesLoaderUtils.loadAllProperties(GIT_PROPERTIES);
            GitInfo gitInfo = new GitInfo();
            gitInfo.setGitBranch(props.getProperty(GIT_BRANCH));
            gitInfo.setGitCommitIdAbbrev(props.getProperty(GIT_COMMIT_ID_ABBREV));
            gitInfo.setGitCommitTime(props.getProperty(GIT_COMMIT_TIME));
            gitInfo.setGitBuildTime(props.getProperty(GIT_BUILD_TIME));
            return gitInfo;
        } catch (IOException ex) {
            return unknown();
        }
    }

    private static GitInfo unknown() {
        GitInfo gitInfo = new GitInfo();
        gitInfo.setGitBranch(GIT_PROPERTIES_NOT_EXIST);
        gitInfo.setGitCommitIdAbbrev(GIT_PROPERTIES_NOT_EXIST);
        gitInfo.setGitCommitTime(GIT_PROPERTIES_NOT_EXIST);
        gitInfo.setGitBuildTime(GIT_PROPERTIES_NOT_EXIST);
        return gitInfo;
    }
}
