package com.lazan.simpleioc;

public interface ServiceBinder {
	<T> ServiceBindOptions bind(Class<T> serviceType);
	<T, C extends T> ServiceBindOptions bind(Class<T> serviceType, Class<C> concreteType);
	<T> ServiceBindOptions bind(Class<T> serviceType, T service);
	<T, C extends T> ServiceBindOptions bind(Class<T> serviceType, ServiceBuilder<C> builder);
	<T, C extends T> ServiceBindOptions override(Class<T> serviceType, Class<C> concreteType);
	<T> ServiceBindOptions override(Class<T> serviceType, T service);
	<T, C extends T> ServiceBindOptions override(Class<T> serviceType, ServiceBuilder<C> builder);
}
