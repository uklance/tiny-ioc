package com.lazan.simpleioc.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.lazan.simpleioc.IocException;
import com.lazan.simpleioc.ServiceBuilder;
import com.lazan.simpleioc.ServiceBuilderContext;
import com.lazan.simpleioc.internal.ServiceRegistryImpl.ServiceBuilderContextImpl;

class ServicePointer {
	private final String serviceId;
	private final Class<?> serviceType;
	private final ServiceBuilder<?> builder;
	private volatile Object service;
	
	public ServicePointer(String serviceId, Class<?> serviceType, ServiceBuilder<?> builder) {
		super();
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		this.builder = builder;
	}

	public synchronized Object get(ServiceRegistryImpl registry) {
		if (service == null) {
			Set<String> serviceIdStack = registry.getServiceIdStack();
			if (serviceIdStack.contains(serviceId)) {
				List<String> references = new LinkedList<>(serviceIdStack);
				references.add(serviceId);
				throw new IocException("Circular dependency reference detected %s", references);
			}
			ServiceRegistryImpl registryWrapper = new ServiceRegistryImpl(registry, serviceId);
			ServiceBuilderContext context = new ServiceBuilderContextImpl(registryWrapper, serviceId, serviceType);
			service = builder.build(context);
		}
		return service;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
}