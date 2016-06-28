package com.lazan.tinyioc;

public interface ServiceDecorator<T> {
	T decorate(T candidate, ServiceBuilderContext<T> delegate);
}
