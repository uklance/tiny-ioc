package com.lazan.tinyioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.tinyioc.MappedConfiguration;
import com.lazan.tinyioc.ServiceBuilder;

public class MappedConfigurationImpl<K, V> implements MappedConfiguration<K, V> {
	public static class Entry<K, V> {
		private String contributionId;
		private ServiceBuilder<? extends K> keyBuilder;
		private ServiceBuilder<? extends V> valueBuilder;
		
		public Entry(String contributionId, ServiceBuilder<? extends K> keyBuilder,
				ServiceBuilder<? extends V> valueBuilder) {
			super();
			this.contributionId = contributionId;
			this.keyBuilder = keyBuilder;
			this.valueBuilder = valueBuilder;
		}
		
		public String getContributionId() {
			return contributionId;
		}
		
		public ServiceBuilder<? extends K> getKeyBuilder() {
			return keyBuilder;
		}
		
		public ServiceBuilder<? extends V> getValueBuilder() {
			return valueBuilder;
		}
	}
	
	private List<Entry<K, V>> entries = new LinkedList<>();

	@Override
	public void add(String contributionId, K key, ServiceBuilder<? extends V> builder) {
		entries.add(new Entry<K, V>(contributionId, new ConstantServiceBuilder<K>(key), builder));
	}

	@Override
	public void add(String contributionId, K key, V value) {
		add(contributionId, key, new ConstantServiceBuilder<>(value));
	}

	@Override
	public void add(String contributionId, K key, Class<? extends V> type) {
		add(contributionId, key, new InjectionServiceBuilder<>(type));
	}
	
	public List<Entry<K, V>> getEntries() {
		return entries;
	}
}
