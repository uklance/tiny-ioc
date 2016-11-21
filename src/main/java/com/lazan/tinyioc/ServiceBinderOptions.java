package com.lazan.tinyioc;

public interface ServiceBinderOptions {
	ServiceBinderOptions withServiceId(String serviceId);
	ServiceBinderOptions withOrderedContribution(Class<?> contributionType);
	ServiceBinderOptions withUnorderedContribution(Class<?> contributionType);
	ServiceBinderOptions withMappedContribution(Class<?> keyType, Class<?> valueType);
	ServiceBinderOptions eagerLoad();
}
