package com.lazan.tinyioc.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ServiceBuilderContextImpl implements ServiceBuilderContext {
	private final ServiceRegistry registry;
	private final String serviceId;
	private final Class<?> serviceType;
	private ContributionType contributionType;
	private Class<?> contributionKeyType;
	private Class<?> contributionValueType;
	private Collection unorderedContributions;
	private List orderedContributions;
	private Map mappedContributions;
	
	public ServiceBuilderContextImpl(ServiceRegistry registry, String serviceId, Class<?> serviceType) {
		super();
		this.registry = registry;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}
	
	void setOrderedConfiguration(Class<?> valueType, List contributions) {
		this.contributionType = ContributionType.ORDERED;
		this.contributionValueType = valueType;
		this.orderedContributions = contributions;
	}

	void setUnorderedConfiguration(Class<?> valueType, Collection contributions) {
		this.contributionType = ContributionType.UNORDERED;
		this.contributionValueType = valueType;
		this.unorderedContributions = contributions;
	}

	void setMappedConfiguration(Class<?> keyType, Class<?> valueType, Map contributions) {
		this.contributionType = ContributionType.MAPPED;
		this.contributionKeyType = keyType;
		this.contributionValueType = valueType;
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
	public ContributionType getContributionType() {
		return contributionType;
	}
	
	@Override
	public <K, V> Map<K, V> getMappedContributions(Class<K> keyType, Class<V> valueType) {
		if (contributionType != ContributionType.MAPPED) throw new IocException("Cannot get mapped contributions for service %s (contributionType=%s)", serviceId, contributionType);
		return mappedContributions;
	}
	
	@Override
	public <V> List<V> getOrderedContributions(Class<V> valueType) {
		if (contributionType != ContributionType.ORDERED) throw new IocException("Cannot get ordered contributions for service %s (contributionType=%s)", serviceId, contributionType);
		return orderedContributions;
	}
	
	@Override
	public <V> Collection<V> getUnorderedContributions(Class<V> valueType) {
		if (contributionType != ContributionType.UNORDERED) throw new IocException("Cannot get unordered contributions for service %s (contributionType=%s)", serviceId, contributionType);
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