package com.lazan.simpleioc;

public interface ServiceBuilderContext {
	String getServiceId();
	Class<?> getServiceType();
	ServiceRegistry getServiceRegistry();
}
