package com.lazan.tinyioc.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceDecorator;

public class ServiceReference<T> {
	private final String serviceId;
	private final Class<T> serviceType;
	private final ServiceBuilder<T> builder;
	private final ServiceDecorator<T> decorator;
	private volatile Object service;
	
	public ServiceReference(String serviceId, Class<T> serviceType, ServiceBuilder<T> builder, ServiceDecorator<T> decorator) {
		super();
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		this.builder = builder;
		this.decorator = decorator;
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
			ServiceBuilderContext<T> context = new ServiceBuilderContextImpl<T>(registryWrapper, serviceId, serviceType);
			
			T candidate = builder.build(context);
			if (decorator != null) {
				service = decorator.decorate(candidate, context);
			} else {
				service = candidate;
			}
		}
		return service;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<T> getServiceType() {
		return serviceType;
	}
}