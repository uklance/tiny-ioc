package com.lazan.tinyioc.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@SuppressWarnings("rawtypes")
public class ServiceBuilderContextImpl<T> implements ServiceBuilderContext<T> {
	private final ServiceRegistry registry;
	private final String serviceId;
	private final Class<T> serviceType;
	private ContributionType contributionType;
	private Class<?> contributionKeyType;
	private Class<?> contributionValueType;
	private Collection<Object> unorderedContributions;
	private List<Object> orderedContributions;
	private Map<Object, Object> mappedContributions;
	
	public ServiceBuilderContextImpl(ServiceRegistry registry, String serviceId, Class<T> serviceType) {
		super();
		this.registry = registry;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}
	
	void setOrderedConfiguration(Class<?> contributionValueType, List<Object> contributions) {
		this.contributionType = ContributionType.ORDERED;
		this.contributionValueType = contributionValueType;
		this.orderedContributions = contributions;
	}

	void setUnorderedConfiguration(Class<?> contributionValueType, Collection<Object> contributions) {
		this.contributionType = ContributionType.UNORDERED;
		this.contributionValueType = contributionValueType;
		this.unorderedContributions = contributions;
	}

	void setMappedConfiguration(Class<?> contributionKeyType, Class<?> contributionValueType, Map<Object, Object> contributions) {
		this.contributionType = ContributionType.MAPPED;
		this.contributionValueType = contributionValueType;
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
	public Class<T> getServiceType() {
		return serviceType;
	}
	
	@Override
	public ContributionType getContributionType() {
		return contributionType;
	}
	
	@Override
	public Map getMappedContributions() {
		if (contributionType != ContributionType.MAPPED) throw new IocException("");
		return mappedContributions;
	}
	
	@Override
	public List getOrderedContributions() {
		if (contributionType != ContributionType.ORDERED) throw new IocException("");
		return orderedContributions;
	}
	
	@Override
	public Collection getUnorderedContributions() {
		if (contributionType != ContributionType.UNORDERED) throw new IocException("");
		return unorderedContributions;
	}
	
	@Override
	public Class<?> getContributionKeyType() {
		return contributionKeyType;
	}
	
	@Override
	public Class<?> getContributionValueType() {
		return contributionValueType;
	}
}