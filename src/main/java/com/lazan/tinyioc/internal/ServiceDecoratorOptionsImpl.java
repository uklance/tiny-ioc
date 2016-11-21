package com.lazan.tinyioc.internal;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;

public class ServiceDecoratorOptionsImpl implements ServiceDecoratorOptions, Comparable<ServiceDecoratorOptionsImpl> {
	private static final AtomicLong NEXT_DEFAULT_ORDER = new AtomicLong(0);

	private final Class<?> serviceType;
	private final ServiceDecorator<?> serviceDecorator;
	private final String decoratorId;
	private String serviceId;
	private Set<String> before;
	private Set<String> after;
	private final long defaultOrder;

	public ServiceDecoratorOptionsImpl(Class<?> serviceType, String decoratorId, ServiceDecorator<?> serviceDecorator) {
		super();
		this.serviceType = serviceType;
		this.decoratorId = decoratorId;
		this.serviceDecorator = serviceDecorator;
		this.defaultOrder = NEXT_DEFAULT_ORDER.getAndIncrement();
	}

	@Override
	public ServiceDecoratorOptions withServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}
	
	@Override
	public ServiceDecoratorOptionsImpl before(String... decoratorIds) {
		if (decoratorIds != null && decoratorIds.length > 0) {
			if (before == null) before = new LinkedHashSet<>();
			before.addAll(Arrays.asList(decoratorIds));
		}
		return this;
	}

	@Override
	public ServiceDecoratorOptionsImpl after(String... decoratorIds) {
		if (decoratorIds != null && decoratorIds.length > 0) {
			if (after == null) after = new LinkedHashSet<>();
			after.addAll(Arrays.asList(decoratorIds));
		}
		return this;
	}
	
	@Override
	public int compareTo(ServiceDecoratorOptionsImpl other) {
		boolean isBefore = before != null && (before.contains(other.getDecoratorId()) || before.contains("*"));
		if (!isBefore) {
			isBefore = other.after != null && (other.after.contains(this.getDecoratorId()) || other.after.contains("*"));
		}
		boolean isAfter = after != null && (after.contains(other.getDecoratorId()) || after.contains("*"));
		if (!isAfter) {
			isAfter = other.before != null && (other.before.contains(this.getDecoratorId()) || other.before.contains("*"));
		}
		if (isBefore && isAfter) {
			throw new IocException("Decorator %s cannot be before and after %s", this.getDecoratorId(), other.getDecoratorId());
		}
		return isBefore ? -1 : isAfter ? 1 : Long.compare(this.defaultOrder, other.defaultOrder);
	}
	
	public String getDecoratorId() {
		return decoratorId;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
	
	public ServiceDecorator<?> getServiceDecorator() {
		return serviceDecorator;
	}
}
