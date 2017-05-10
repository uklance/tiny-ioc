package com.lazan.tinyioc.internal;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.OrderedConfiguration;
import com.lazan.tinyioc.OrderedContributionOptions;
import com.lazan.tinyioc.ServiceBuilder;


public class OrderedConfigurationImpl<T> implements OrderedConfiguration<T> {
	public static class Entry<T> implements Comparable<Entry<T>>, OrderedContributionOptions {
		private static final AtomicLong NEXT_DEFAULT_ORDER = new AtomicLong(0);

		private String contributionId;
		private ServiceBuilder<? extends T> builder;
		private Set<String> before;
		private Set<String> after;
		private final long defaultOrder;
		
		public Entry(String contributionId, ServiceBuilder<? extends T> builder) {
			super();
			this.contributionId = contributionId;
			this.builder = builder;
			this.defaultOrder = NEXT_DEFAULT_ORDER.getAndIncrement();
		}
		
		@Override
		public OrderedContributionOptions before(String contributionId) {
			if (before == null) before = new LinkedHashSet<>();
			before.add(contributionId);
			return this;
		}

		@Override
		public OrderedContributionOptions after(String contributionId) {
			if (after == null) after = new LinkedHashSet<>();
			after.add(contributionId);
			return this;
		}
		
		@Override
		public int compareTo(Entry<T> other) {
			boolean isBefore = before != null && (before.contains(other.getContributionId()) || before.contains("*"));
			if (!isBefore) {
				isBefore = other.after != null && (other.after.contains(this.getContributionId()) || other.after.contains("*"));
			}
			boolean isAfter = after != null && (after.contains(other.getContributionId()) || after.contains("*"));
			if (!isAfter) {
				isAfter = other.before != null && (other.before.contains(this.getContributionId()) || other.before.contains("*"));
			}
			if (isBefore && isAfter) {
				throw new IocException("Contribution %s cannot be before and after %s", this.getContributionId(), other.getContributionId());
			}
			return isBefore ? -1 : isAfter ? 1 : Long.compare(this.defaultOrder, other.defaultOrder);
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
	public OrderedContributionOptions add(String contributionId, ServiceBuilder<? extends T> builder) {
		Entry<T> entry = new Entry<>(contributionId, builder);
		entries.add(entry);
		return entry;
	}

	@Override
	public OrderedContributionOptions add(String contributionId, Class<? extends T> type) {
		return add(contributionId, new AutobuildServiceBuilder<>(type));
	}

	@Override
	public OrderedContributionOptions add(String contributionId, T value) {
		return add(contributionId, new ConstantServiceBuilder<>(value));
	}
	
	public List<Entry<T>> getEntries() {
		return entries;
	}
}
