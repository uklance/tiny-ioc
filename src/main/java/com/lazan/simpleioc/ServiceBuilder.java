package com.lazan.simpleioc;

public interface ServiceBuilder<T> {
	T build(ServiceBuilderContext context);
}
