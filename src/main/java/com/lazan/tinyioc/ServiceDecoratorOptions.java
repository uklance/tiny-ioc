package com.lazan.tinyioc;

public interface ServiceDecoratorOptions {
	ServiceDecoratorOptions withServiceId(String serviceId);
	ServiceDecoratorOptions before(String serviceId);
	ServiceDecoratorOptions after(String serviceId);
}
