package com.lazan.tinyioc;

public interface UnorderedContributor<T> {
	void contribute(ServiceBuilderContext context, UnorderedConfiguration<T> configuration);
}
