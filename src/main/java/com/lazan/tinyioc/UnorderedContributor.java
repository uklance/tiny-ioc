package com.lazan.tinyioc;

public interface UnorderedContributor<T> {
	void contribute(UnorderedConfiguration<T> configuration);
}
