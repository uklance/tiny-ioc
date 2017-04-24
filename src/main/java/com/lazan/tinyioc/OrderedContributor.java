package com.lazan.tinyioc;

public interface OrderedContributor<T> {
	void contribute(ServiceBuilderContext context, OrderedConfiguration<T> configuration);
}
