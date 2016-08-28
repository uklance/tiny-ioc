package com.lazan.tinyioc.internal;

public class MappedContributionOptionsImpl extends UnorderedContributionOptionsImpl {
	private final Object key;

	public MappedContributionOptionsImpl(String serviceId, String contributionId, Object key, Object value) {
		super(serviceId, contributionId, value);
		this.key = key;
	}
	
	public Object getKey() {
		return key;
	}
}
