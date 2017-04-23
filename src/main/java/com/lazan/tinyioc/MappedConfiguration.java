package com.lazan.tinyioc;

public interface MappedConfiguration<K, V> {
	void add(String contributionId, K key, ServiceBuilder<? extends V> builder);
	void add(String contributionId, K key, V value);	
	void add(String contributionId, K key, Class<? extends V> type);
	
	/*
	void override(String contributionId, K key, ServiceBuilder<? extends V> builder);
	void override(String contributionId, K key, V value);	
	void override(String contributionId, K key, Class<? extends V> type);
	*/
}
