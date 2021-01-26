package com.stark.druid.admin.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.stat.DruidStatServiceMBean;
import com.alibaba.fastjson.JSONArray;
import com.stark.commons.lang.util.HttpUtil;
import com.stark.commons.lang.util.Utils;
import com.stark.druid.admin.boot.properties.DruidAdminProperties;
import com.stark.druid.admin.domain.Service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DruidAdminService implements DruidStatServiceMBean {
	
    private DiscoveryClient discoveryClient;
    
    private DruidAdminProperties druidAdminProperties;
    
    public String service(String url) {
    	if (url.equals("/service.json")) {
    		List<Service> services = getServices();
    		return DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_SUCCESS, JSONArray.toJSON(services));
    	}
    	
        Map<String, String> parameters = DruidStatService.getParameters(url);
        String serviceId = parameters.get("serviceId");
        String serviceInstanceId = parameters.get("instanceId");
        parameters.remove("serviceId");
        parameters.remove("instanceId");
        url = StringUtils.substringBefore(url, "?") + "?" + Utils.getQueryString(parameters);
        return delegate(serviceId, serviceInstanceId, url);
    }
    
    private List<Service> getServices() {
    	List<Service> services = discoveryClient.getServices()
    			.stream()
	    		.filter(serviceId -> druidAdminProperties.getApplications().stream().anyMatch(application -> application.equalsIgnoreCase(serviceId)))
	    		.map(serviceId -> {
	    			Service service = new Service(serviceId);
	    			List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
	    			service.setInstances(instances);
	    			return service;
	    		})
	    		.collect(Collectors.toList());
    	return services;
    }
    
    private String getInstanceId(ServiceInstance instance) {
		String instanceId = instance.getInstanceId();
        if (instanceId == null) {
            instanceId = instance.getMetadata().get("nacos.instanceId").replaceAll("#", "-").replaceAll("@@", "-");
        }
        return instanceId;
    }
    
    private String delegate(String serviceId, String serviceInstanceId, String url) {
    	List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
    	ServiceInstance instance = instances.stream().filter(it -> serviceInstanceId.equals(getInstanceId(it))).findFirst().orElse(null);
    	if (instance == null) {
    		return DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR, "No such a service instance: serviceId=" + serviceId + ",serviceInstanceId=" + serviceInstanceId + ".");
    	}
    	url = instance.getScheme() + "://" + instance.getHost() + ":" + instance.getPort() + "/druid" + url;
    	
    	String responseJson = "";
    	try {
			HttpResponse response = HttpUtil.doGet(url, null, null);
			responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			log.error("Delegate to service instance error: serviceId={},serviceInstanceId={},url={}",
					serviceId, serviceInstanceId, url, e);
			return DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR, "Delegate to service instance error : " + e.getMessage());
		}
    	return responseJson;
    }
    
}
