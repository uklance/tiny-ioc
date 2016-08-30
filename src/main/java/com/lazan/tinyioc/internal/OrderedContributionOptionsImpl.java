package com.lazan.tinyioc.internal;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.OrderedContributionOptions;

public class OrderedContributionOptionsImpl extends UnorderedContributionOptionsImpl implements OrderedContributionOptions, Comparable<OrderedContributionOptionsImpl> {
	private static final AtomicLong NEXT_DEFAULT_ORDER = new AtomicLong(0);

	private Set<String> before;
	private Set<String> after;
	private final long defaultOrder;
	
	public OrderedContributionOptionsImpl(String serviceId, String contributionId, Object value) {
		super(serviceId, contributionId, value);
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
	public int compareTo(OrderedContributionOptionsImpl other) {
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
}
