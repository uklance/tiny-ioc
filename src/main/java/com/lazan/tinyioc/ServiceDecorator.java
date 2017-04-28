package com.lazan.tinyioc;

public interface ServiceDecorator<T> {
	T decorate(ServiceBuilderContext context, T delegate);
}
