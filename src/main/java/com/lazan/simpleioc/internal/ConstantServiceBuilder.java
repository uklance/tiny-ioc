package com.lazan.simpleioc.internal;

import com.lazan.simpleioc.ServiceBuilder;
import com.lazan.simpleioc.ServiceBuilderContext;

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