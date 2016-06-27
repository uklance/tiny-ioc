package com.lazan.tinyioc;

public interface ServiceBinder {
	<T> ServiceBinderOptions bind(Class<T> serviceType);
	<T, C extends T> ServiceBinderOptions bind(Class<T> serviceType, Class<C> concreteType);
	<T> ServiceBinderOptions bind(Class<T> serviceType, T service);
	<T, C extends T> ServiceBinderOptions bind(Class<T> serviceType, ServiceBuilder<C> builder);
	<T, C extends T> ServiceBinderOptions override(Class<T> serviceType, Class<C> concreteType);
	<T> ServiceBinderOptions override(Class<T> serviceType, T service);
	<T, C extends T> ServiceBinderOptions override(Class<T> serviceType, ServiceBuilder<C> builder);
	<T> ServiceDecoratorOptions decorate(Class<T> serviceType, ServiceDecorator<T> decorator);
}
