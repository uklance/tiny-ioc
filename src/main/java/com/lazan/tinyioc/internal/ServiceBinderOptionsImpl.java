package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;

public class ServiceBinderOptionsImpl implements ServiceBinderOptions {
	private final Class<?> serviceType;
	private final ServiceBuilder<?> serviceBuilder;
	private String serviceId;
	private boolean eagerLoad;

	public ServiceBinderOptionsImpl(Class<?> serviceType, ServiceBuilder<?> serviceBuilder) {
		super();
		this.serviceType = serviceType;
		this.serviceBuilder = serviceBuilder;
	}

	@Override
	public ServiceBinderOptions withServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}
	
	@Override
	public ServiceBinderOptions eagerLoad() {
		this.eagerLoad = true;
		return this;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
	
	public ServiceBuilder<?> getServiceBuilder() {
		return serviceBuilder;
	}
	
	public boolean isEagerLoad() {
		return eagerLoad;
	}
}