package com.lazan.tinyioc;

public interface ServiceBuilderContext {
	String getServiceId();
	Class<?> getServiceType();
	ServiceRegistry getServiceRegistry();
}
