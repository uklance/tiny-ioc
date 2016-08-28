package com.lazan.tinyioc;

public interface ServiceBinder {
	<T> ServiceBinderOptions bind(Class<T> serviceType);
	<T> ServiceBinderOptions bind(Class<T> serviceType, Class<? extends T> concreteType);
	<T> ServiceBinderOptions bind(Class<T> serviceType, T service);
	<T> ServiceBinderOptions bind(Class<T> serviceType, ServiceBuilder<? extends T> builder);
	<T> ServiceBinderOptions override(Class<T> serviceType, Class<? extends T> concreteType);
	<T> ServiceBinderOptions override(Class<T> serviceType, T service);
	<T> ServiceBinderOptions override(Class<T> serviceType, ServiceBuilder<? extends T> builder);
	<T> ServiceDecoratorOptions decorate(Class<T> serviceType, ServiceDecorator<? extends T> decorator);
	void unorderedContribution(String serviceId, String contributionId, Object value);
	OrderedContributionOptions orderedContribution(String serviceId, String contributionId, Object value);
	void mappedContribution(String serviceId, String contributionId, Object key, Object value);
}
