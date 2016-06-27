package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

public class ServiceBuilderContextImpl implements ServiceBuilderContext {
	private final ServiceRegistry registry;
	private final String serviceId;
	private final Class<?> serviceType;
	
	public ServiceBuilderContextImpl(ServiceRegistry registry, String serviceId, Class<?> serviceType) {
		super();
		this.registry = registry;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}
	
	@Override
	public ServiceRegistry getServiceRegistry() {
		return registry;
	}
	
	@Override
	public Class<?> getServiceType() {
		return serviceType;
	}
}