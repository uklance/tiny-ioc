package com.lazan.tinyioc;

import java.util.Map;
import java.util.Set;

public interface ServiceRegistry {
	Set<String> getServiceIds();
	Set<Class<?>> getServiceTypes();
	<T> T getService(Class<T> serviceType);
	Object getService(String serviceId);
	<T> T getService(String serviceId, Class<T> serviceType);
	<T> Map<String, T> getServices(Class<T> serviceType);
	<T> T autobuild(Class<T> concreteType);
}
