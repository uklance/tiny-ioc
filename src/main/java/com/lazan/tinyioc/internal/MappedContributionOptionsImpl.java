package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceBuilder;

public class MappedContributionOptionsImpl extends UnorderedContributionOptionsImpl {
	private final ServiceBuilder<?> keyBuilder;

	public MappedContributionOptionsImpl(String serviceId, String contributionId, ServiceBuilder<?> keyBuilder, ServiceBuilder<?> valueBuilder) {
		super(serviceId, contributionId, valueBuilder);
		this.keyBuilder = keyBuilder;
	}
	
	public ServiceBuilder<?> getKeyBuilder() {
		return keyBuilder;
	}
}
