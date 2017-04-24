package com.lazan.tinyioc;

public interface MappedContributor<K, V> {
	void contribute(ServiceBuilderContext context, MappedConfiguration<K, V> configuration);
}
