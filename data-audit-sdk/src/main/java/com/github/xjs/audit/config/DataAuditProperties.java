/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 行为审计相关配置属性
 *
 * @author 605162215@qq.com
 * @date 2019/12/4 16:20
 **/
@ConfigurationProperties(prefix = "audit")
@Data
public class DataAuditProperties {

    /**url*/
    private Url url;

    /**分配的app key*/
    private String appKey;

    /**分配的app secfret*/
    private String appSecret;

    private Kakfa kafka;


    public static class Kakfa{
        private String bootstrapServers = "localhost:9092";
        private String fallbackFileName;
        public String getBootstrapServers() {
            return this.bootstrapServers;
        }
        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }
        public String getFallbackFileName() {
            return fallbackFileName;
        }

        public void setFallbackFileName(String fallbackFileName) {
            this.fallbackFileName = fallbackFileName;
        }
    }

    @Data
    public static class Url{

        /**获取初始化数据*/
        private String api;

        /**从权限中心获取用户信息*/
        private String auth;

    }

}
