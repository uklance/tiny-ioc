package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceBuilder;

public class UnorderedContributionOptionsImpl {
	private final String serviceId;
	private final String contributionId;
	private final ServiceBuilder<?> valueBuilder;
	
	public UnorderedContributionOptionsImpl(String serviceId, String contributionId, ServiceBuilder<?> valueBuilder) {
		super();
		this.serviceId = serviceId;
		this.contributionId = contributionId;
		this.valueBuilder = valueBuilder;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getContributionId() {
		return contributionId;
	}
	
	public ServiceBuilder<?> getValueBuilder() {
		return valueBuilder;
	}
}
