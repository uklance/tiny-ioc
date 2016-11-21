package com.lazan.tinyioc.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceDecorator;

public class ServiceReference<T> {
	public class ServiceDependencies {
		private final Class<T> serviceType;
		private final ServiceBuilder<T> builder;
		private final List<ServiceDecorator<T>> decorators;
		private final ContributionType contributionType;
		private final Class<?> contributionKeyType;
		private final Class<?> contributionValueType;
		private final List<UnorderedContributionOptionsImpl> unorderedContributions;
		private final List<OrderedContributionOptionsImpl> orderedContributions;
		private final List<MappedContributionOptionsImpl> mappedContributions;
		public ServiceDependencies(Class<T> serviceType, ServiceBuilder<T> builder, List<ServiceDecorator<T>> decorators,
				ContributionType contributionType, Class<?> contributionKeyType, Class<?> contributionValueType,
				List<UnorderedContributionOptionsImpl> unorderedContributions,
				List<OrderedContributionOptionsImpl> orderedContributions,
				List<MappedContributionOptionsImpl> mappedContributions) {
			super();
			this.serviceType = serviceType;
			this.builder = builder;
			this.decorators = decorators;
			this.contributionType = contributionType;
			this.contributionKeyType = contributionKeyType;
			this.contributionValueType = contributionValueType;
			this.unorderedContributions = unorderedContributions;
			this.orderedContributions = orderedContributions;
			this.mappedContributions = mappedContributions;
		}
	}
	
	private final String serviceId;
	private ServiceDependencies dependencies;
	private volatile Object service;
	
	public ServiceReference(String serviceId, Class<T> serviceType, ServiceBuilder<T> builder,
			List<ServiceDecorator<T>> decorators, ContributionType contributionType, Class<?> contributionKeyType,
			Class<?> contributionValueType, List<UnorderedContributionOptionsImpl> unorderedContributions,
			List<OrderedContributionOptionsImpl> orderedContributions,
			List<MappedContributionOptionsImpl> mappedContributions) {
		super();
		this.serviceId = serviceId;
		this.dependencies = new ServiceDependencies(serviceType, builder, decorators, contributionType, contributionKeyType, contributionValueType, unorderedContributions, orderedContributions, mappedContributions);
	}

	public synchronized Object get(ServiceRegistryImpl registry) {
		if (service == null) {
			Set<String> serviceIdStack = registry.getServiceIdStack();
			if (serviceIdStack.contains(serviceId)) {
				List<String> references = new LinkedList<>(serviceIdStack);
				references.add(serviceId);
				throw new IocException("Circular dependency reference detected %s", references);
			}
			ServiceRegistryImpl registryWrapper = new ServiceRegistryImpl(registry, serviceId);
			ServiceBuilderContextImpl context = new ServiceBuilderContextImpl(registryWrapper, serviceId, dependencies.serviceType);
			
			if (dependencies.contributionType != null) {
				switch (dependencies.contributionType) {
					case MAPPED: 
						context.setMappedContributions(dependencies.contributionKeyType, dependencies.contributionValueType, buildContributionMap(context));
						break;
					case ORDERED:
						context.setOrderedContributions(dependencies.contributionValueType, buildContributionList(context));
						break;
					case UNORDERED:
						context.setUnorderedContributions(dependencies.contributionValueType, buildContributionCollection(context));
						break;
					default:
						throw new IocException("Unsupported contributiontype %s", dependencies.contributionType);
				}
			}
			T candidate = dependencies.builder.build(context);
			if (dependencies.decorators != null) {
				for (ServiceDecorator<T> decorator : dependencies.decorators) {
					candidate = decorator.decorate(candidate, context);
				}
			}
			service = candidate;
			
			// allow dependencies to be garbage collected
			dependencies = null;
		}
		return service;
	}
	
	private Collection<Object> buildContributionCollection(ServiceBuilderContext context) {
		if (dependencies.unorderedContributions == null || dependencies.unorderedContributions.isEmpty()) return Collections.emptyList();
		List<Object> values = new LinkedList<>();
		for (UnorderedContributionOptionsImpl current : dependencies.unorderedContributions) {
			values.add(current.getValueBuilder().build(context));
		}
		return Collections.unmodifiableList(values);
	}

	private List<Object> buildContributionList(ServiceBuilderContext context) {
		if (dependencies.orderedContributions == null || dependencies.orderedContributions.isEmpty()) return Collections.emptyList();
		List<Object> values = new LinkedList<>();
		List<OrderedContributionOptionsImpl> copy = new LinkedList<>(dependencies.orderedContributions);
		Collections.sort(copy);
		for (OrderedContributionOptionsImpl current : copy) {
			values.add(current.getValueBuilder().build(context));
		}
		return Collections.unmodifiableList(values);
	}

	private Map<Object, Object> buildContributionMap(ServiceBuilderContext context) {
		if (dependencies.mappedContributions == null || dependencies.mappedContributions.isEmpty()) return Collections.emptyMap();
		Map<Object, Object> values = new LinkedHashMap<>();
		for (MappedContributionOptionsImpl current : dependencies.mappedContributions) {
			Object key = current.getKeyBuilder().build(context);
			if (values.containsKey(key)) throw new IocException("Duplicate contribution key %s", key);
			values.put(key, current.getValueBuilder().build(context));
		}
		return Collections.unmodifiableMap(values);
	}

	public String getServiceId() {
		return serviceId;
	}
}