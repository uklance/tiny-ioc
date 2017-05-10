package com.lazan.tinyioc;

public interface Autobuilder {
	<T> T autobuild(ServiceRegistry registry, Class<T> concreteType);
	<T> T autobuild(ServiceBuilderContext context, Class<T> concreteType);
}
