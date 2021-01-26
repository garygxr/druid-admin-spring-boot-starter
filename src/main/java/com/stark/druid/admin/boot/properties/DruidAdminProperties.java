package com.stark.druid.admin.boot.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Druid Admin 配置项。 
 * @author Ben
 * @since 1.0.0
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.druid.admin")
public class DruidAdminProperties {

    /** 需要监控的服务 */
    private List<String> applications;
    
    /** 登录用户名 */
    private String loginUsername;
    
    /** 登录密码 */
    private String loginPassword;

}
