package com.lazan.tinyioc.internal;

public class UnorderedContributionOptionsImpl {
	private final String serviceId;
	private final String contributionId;
	private final Object value;
	
	public UnorderedContributionOptionsImpl(String serviceId, String contributionId, Object value) {
		super();
		this.serviceId = serviceId;
		this.contributionId = contributionId;
		this.value = value;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getContributionId() {
		return contributionId;
	}

	public Object getValue() {
		return value;
	}
}
