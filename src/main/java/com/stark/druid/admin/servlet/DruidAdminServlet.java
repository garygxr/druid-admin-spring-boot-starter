package com.stark.druid.admin.servlet;

import javax.servlet.ServletException;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.alibaba.druid.support.http.ResourceServlet;
import com.stark.druid.admin.boot.properties.DruidAdminProperties;
import com.stark.druid.admin.service.DruidAdminService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author linchtech
 * @date 2020-09-16 11:10
 **/
@Slf4j
public class DruidAdminServlet extends ResourceServlet {

	private static final long serialVersionUID = -4087534393503211407L;
	
	private DiscoveryClient discoveryClient;
	    
	private DruidAdminProperties druidAdminProperties;
	
	private DruidAdminService druidAdminService;

    public DruidAdminServlet(DiscoveryClient discoveryClient, DruidAdminProperties druidAdminProperties) {
        super("templates");
        this.discoveryClient = discoveryClient;
        this.druidAdminProperties = druidAdminProperties;
    }
    
    public void init() throws ServletException {
        log.info("init MonitorViewServlet");
        super.init();
        druidAdminService = new DruidAdminService(discoveryClient, druidAdminProperties);
    }

    @Override
    protected String process(String url) {
        return druidAdminService.service(url);
    }

}
