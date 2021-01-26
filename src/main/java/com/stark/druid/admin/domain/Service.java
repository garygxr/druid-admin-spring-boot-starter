package com.stark.druid.admin.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 微服务。
 * @author Ben
 * @since 1.0.0
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Service implements Serializable {
	
	private static final long serialVersionUID = -1385427412604847423L;

	@NonNull
	private String id;
	
	private List<ServiceInstance> instances = new ArrayList<>();

}
