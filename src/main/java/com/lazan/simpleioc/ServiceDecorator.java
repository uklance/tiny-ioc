package com.lazan.simpleioc;

public interface ServiceDecorator<T> {
	T decorate(T candidate, ServiceBuilderContext context);
}
