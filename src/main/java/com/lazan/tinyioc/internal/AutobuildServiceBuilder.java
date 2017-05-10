package com.lazan.tinyioc.internal;

import com.lazan.tinyioc.Autobuilder;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;

public class AutobuildServiceBuilder<T> implements ServiceBuilder<T> {
	private final Class<T> concreteType;
	
	public AutobuildServiceBuilder(Class<T> concreteType) {
		super();
		this.concreteType = concreteType;
	}

	@Override
	public T build(ServiceBuilderContext context) {
		Autobuilder autobuilder = context.getServiceRegistry().getService(Autobuilder.class);
		return autobuilder.autobuild(context, concreteType);
	}
}
