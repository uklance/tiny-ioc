package com.lazan.simpleioc.internal;

import com.lazan.simpleioc.ServiceBindOptions;
import com.lazan.simpleioc.ServiceBuilder;

public class ServiceBindOptionsImpl implements ServiceBindOptions {
	private final Class<?> serviceType;
	private final ServiceBuilder<?> builder;
	private String serviceId;
	
	public ServiceBindOptionsImpl(Class<?> serviceType, ServiceBuilder<?> builder) {
		super();
		this.serviceType = serviceType;
		this.builder = builder;
	}

	@Override
	public ServiceBindOptions withServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
	
	public ServiceBuilder<?> getServiceBuilder() {
		return builder;
	}
}