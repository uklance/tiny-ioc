package com.lazan.tinyioc;

public interface ServiceBuilderContext<T> {
	String getServiceId();
	Class<T> getServiceType();
	ServiceRegistry getServiceRegistry();
}
