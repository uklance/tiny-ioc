package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;

public class ConstantServiceBuilder<T> implements ServiceBuilder<T> {
	private final T service;
	
	public ConstantServiceBuilder(T service) {
		super();
		this.service = service;
	}

	@Override
	public T build(ServiceBuilderContext context) {
		return service;
	}
}