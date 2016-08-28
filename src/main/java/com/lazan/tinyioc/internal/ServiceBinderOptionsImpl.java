package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;

public class ServiceBinderOptionsImpl implements ServiceBinderOptions {
	private final Class<?> serviceType;
	private final ServiceBuilder<?> serviceBuilder;
	private String serviceId;
	private ContributionType contributionType;
	private Class<?> contributionKeyType;
	private Class<?> contributionValueType;
	
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
	public ServiceBinderOptions withMappedContribution(Class<?> keyType, Class<?> valueType) {
		if (contributionType != null) throw new IocException("Multiple contribution types specified");
		contributionType = ContributionType.MAPPED;
		contributionKeyType = keyType;
		contributionValueType = valueType;
		return this;
	}
	
	@Override
	public ServiceBinderOptions withOrderedContribution(Class<?> contributionType) {
		if (this.contributionType != null) throw new IocException("Multiple contribution types specified");
		this.contributionType = ContributionType.ORDERED;
		this.contributionValueType = contributionType;
		return this;
	}
	
	@Override
	public ServiceBinderOptions withUnorderedContribution(Class<?> contributionType) {
		if (contributionType != null) throw new IocException("Multiple contribution types specified");
		this.contributionType = ContributionType.UNORDERED;
		this.contributionValueType = contributionType;
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
	
	public ContributionType getContributionType() {
		return contributionType;
	}
	
	public Class<?> getContributionKeyType() {
		return contributionKeyType;
	}
	
	public Class<?> getContributionValueType() {
		return contributionValueType;
	}
}