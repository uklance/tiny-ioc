package com.lazan.tinyioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.UnorderedConfiguration;

public class UnorderedConfigurationImpl<T> implements UnorderedConfiguration<T> {
	public static class Entry<T> {
		private String contributionId;
		private ServiceBuilder<? extends T> builder;
		
		public Entry(String contributionId, ServiceBuilder<? extends T> builder) {
			super();
			this.contributionId = contributionId;
			this.builder = builder;
		}
		
		public String getContributionId() {
			return contributionId;
		}
		
		public ServiceBuilder<? extends T> getValueBuilder() {
			return builder;
		}
	}
	
	private List<Entry<T>> entries = new LinkedList<>();
	
	@Override
	public void add(String contributionId, ServiceBuilder<? extends T> builder) {
		Entry<T> entry = new Entry<T>(contributionId, builder);
		entries.add(entry);
	}

	@Override
	public void add(String contributionId, Class<? extends T> type) {
		add(contributionId, new InjectionServiceBuilder<>(type));
	}

	@Override
	public void add(String contributionId, T value) {
		add(contributionId, new ConstantServiceBuilder<>(value));
	}
	
	public List<Entry<T>> getEntries() {
		return entries;
	}
}
