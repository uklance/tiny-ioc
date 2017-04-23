package com.lazan.tinyioc;

public interface MappedContributor<K, V> {
	void contribute(MappedConfiguration<K, V> configuration);
}
