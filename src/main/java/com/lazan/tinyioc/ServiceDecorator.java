package com.lazan.tinyioc;

public interface ServiceDecorator<T> {
	T decorate(T delegate, ServiceBuilderContext context);
}
