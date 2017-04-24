package com.lazan.tinyioc;

public interface ServiceBinderOptions {
	ServiceBinderOptions withServiceId(String serviceId);
	ServiceBinderOptions eagerLoad();
}
