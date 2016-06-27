package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;

public class ServiceDecoratorOptionsImpl implements ServiceDecoratorOptions {
	private final Class<?> serviceType;
	private final ServiceDecorator<?> serviceDecorator;
	private String serviceId;
	
	public ServiceDecoratorOptionsImpl(Class<?> serviceType, ServiceDecorator<?> serviceDecorator) {
		super();
		this.serviceType = serviceType;
		this.serviceDecorator = serviceDecorator;
	}

	@Override
	public ServiceDecoratorOptions withId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
	
	public ServiceDecorator<?> getServiceDecorator() {
		return serviceDecorator;
	}
}
