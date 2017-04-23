package com.lazan.tinyioc;

public interface ServiceBinder {
	<T> ServiceBinderOptions bind(Class<T> serviceType);
	<T> ServiceBinderOptions bind(Class<T> serviceType, Class<? extends T> concreteType);
	<T> ServiceBinderOptions bind(Class<T> serviceType, T service);
	<T> ServiceBinderOptions bind(Class<T> serviceType, ServiceBuilder<? extends T> builder);
	<T> ServiceBinderOptions override(Class<T> serviceType, Class<? extends T> concreteType);
	<T> ServiceBinderOptions override(Class<T> serviceType, T service);
	<T> ServiceBinderOptions override(Class<T> serviceType, ServiceBuilder<? extends T> builder);
	<T> ServiceDecoratorOptions decorate(Class<T> serviceType, String decoratorId, ServiceDecorator<? extends T> decorator);
	void contribute(String serviceId, OrderedContributor<?> contributor);
	void contribute(Class<?> serviceType, OrderedContributor<?> contributor);
	void contribute(String serviceId, UnorderedContributor<?> contributor);
	void contribute(Class<?> serviceType, UnorderedContributor<?> contributor);
	void contribute(String serviceId, MappedContributor<?, ?> contributor);
	void contribute(Class<?> serviceType, MappedContributor<?, ?> contributor);
}
