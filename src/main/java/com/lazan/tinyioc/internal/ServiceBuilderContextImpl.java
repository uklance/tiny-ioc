package com.lazan.tinyioc.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@SuppressWarnings("rawtypes")
public class ServiceBuilderContextImpl implements ServiceBuilderContext {
	private final ServiceRegistry registry;
	private final String serviceId;
	private final Class<?> serviceType;
	private Collection unorderedContributions;
	private List orderedContributions;
	private Map mappedContributions;
	
	public ServiceBuilderContextImpl(ServiceRegistry registry, String serviceId, Class<?> serviceType) {
		super();
		this.registry = registry;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}
	
	void setOrderedContributions(List contributions) {
		this.orderedContributions = contributions;
	}

	void setUnorderedContributions(Collection contributions) {
		this.unorderedContributions = contributions;
	}

	void setMappedContributions(Map contributions) {
		this.mappedContributions = contributions;
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
	
	@Override
	public Map getMappedContributions() {
		if (mappedContributions == null) {
			throw new IocException("Circular dependency encountered accessing mappedContributions for " + getServiceId());
		}
		return mappedContributions;
	}
	
	@Override
	public List getOrderedContributions() {
		if (orderedContributions == null) {
			throw new IocException("Circular dependency encountered accessing orderedContributions for " + getServiceId());
		}
		return orderedContributions;
	}
	
	@Override
	public Collection getUnorderedContributions() {
		if (unorderedContributions == null) {
			throw new IocException("Circular dependency encountered accessing unorderedContributions for " + getServiceId());
		}
		return unorderedContributions;
	}
}