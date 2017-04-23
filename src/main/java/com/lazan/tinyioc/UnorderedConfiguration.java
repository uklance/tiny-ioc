package com.lazan.tinyioc;

public interface UnorderedConfiguration<T> {
	void add(String contributionId, ServiceBuilder<? extends T> builder);
	void add(String contributionId, T value);	
	void add(String contributionId, Class<? extends T> type);
	/*
	void override(String contributionId, ServiceBuilder<? extends T> builder);
	void override(String contributionId, T value);	
	void override(String contributionId, Class<? extends T> type);
	*/
}
