package com.stark.druid.admin.boot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.stark.druid.admin.boot.properties.DruidAdminProperties;
import com.stark.druid.admin.servlet.DruidAdminServlet;

@Configuration
@ComponentScan(basePackages = "com.stark.druid.admin")
@EnableConfigurationProperties(DruidAdminProperties.class)
public class DruidAdminAutoConfig {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean statViewServletRegistrationBean(DiscoveryClient discoveryClient, DruidAdminProperties properties) {
		ServletRegistrationBean registrationBean = new ServletRegistrationBean();
		registrationBean.setServlet(new DruidAdminServlet(discoveryClient, properties));
		registrationBean.addUrlMappings("/druid/*");
		if (properties.getLoginUsername() != null) {
			registrationBean.addInitParameter("loginUsername", properties.getLoginUsername());
		}
		if (properties.getLoginPassword() != null) {
			registrationBean.addInitParameter("loginPassword", properties.getLoginPassword());
		}
		return registrationBean;
	}

}
