package com.lazan.tinyioc.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.MappedContributor;
import com.lazan.tinyioc.OrderedContributor;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.UnorderedContributor;

public class ServiceReference<T> {
	public class ServiceDependencies {
		private final Class<T> serviceType;
		private final ServiceBuilder<T> builder;
		private final List<ServiceDecorator<T>> decorators;
		private final List<UnorderedContributor<?>> unorderedContributions;
		private final List<OrderedContributor<?>> orderedContributions;
		private final List<MappedContributor<?,?>> mappedContributions;
		public ServiceDependencies(Class<T> serviceType, ServiceBuilder<T> builder, List<ServiceDecorator<T>> decorators,
				List<UnorderedContributor<?>> unorderedContributions,
				List<OrderedContributor<?>> orderedContributions,
				List<MappedContributor<?, ?>> mappedContributions) {
			super();
			this.serviceType = serviceType;
			this.builder = builder;
			this.decorators = decorators;
			this.unorderedContributions = unorderedContributions;
			this.orderedContributions = orderedContributions;
			this.mappedContributions = mappedContributions;
		}
	}
	
	private final String serviceId;
	private final boolean eagerLoad;
	private ServiceDependencies dependencies;
	private volatile Object service;
	
	public ServiceReference(String serviceId, Class<T> serviceType, ServiceBuilder<T> builder, boolean eagerLoad,
			List<ServiceDecorator<T>> decorators, 
			List<UnorderedContributor<?>> unorderedContributions,
			List<OrderedContributor<?>> orderedContributions,
			List<MappedContributor<?, ?>> mappedContributions) {
		super();
		this.serviceId = serviceId;
		this.eagerLoad = eagerLoad;
		this.dependencies = new ServiceDependencies(
				serviceType, builder, decorators, 
				unorderedContributions, orderedContributions, mappedContributions
		);
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
			context.setMappedContributions(buildMappedContributions(context));
			context.setOrderedContributions(buildOrderedContributions(context));
			context.setUnorderedContributions(buildUnorderedContributions(context));
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
	
	public void init(ServiceRegistryImpl registry) {
		if (eagerLoad) {
			get(registry);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<Object> buildUnorderedContributions(ServiceBuilderContext context) {
		if (dependencies.unorderedContributions == null) {
			return Collections.emptyList();
		}
		UnorderedConfigurationImpl configuration = new UnorderedConfigurationImpl();
		for (UnorderedContributor contributor : dependencies.unorderedContributions) {
			contributor.contribute(context, configuration);
		}
		Map<String, UnorderedConfigurationImpl.Entry> entryMap = new LinkedHashMap<>();
		List<UnorderedConfigurationImpl.Entry> entries = configuration.getEntries();
		for (UnorderedConfigurationImpl.Entry entry : entries) {
			entryMap.put(entry.getContributionId(), entry);
		}
		List<Object> values = new ArrayList<>(entryMap.size());
		for (UnorderedConfigurationImpl.Entry entry : entryMap.values()) {
			values.add(entry.getValueBuilder().build(context));
		}
		return Collections.unmodifiableCollection(values);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Object> buildOrderedContributions(ServiceBuilderContext context) {
		if (dependencies.orderedContributions == null) {
			return Collections.emptyList();
		}
		OrderedConfigurationImpl configuration = new OrderedConfigurationImpl();
		for (OrderedContributor contributor : dependencies.orderedContributions) {
			contributor.contribute(context, configuration);
		}
		List<OrderedConfigurationImpl.Entry> entries = configuration.getEntries();
		Collections.sort(entries);
		Map<String, OrderedConfigurationImpl.Entry> entryMap = new LinkedHashMap<>();
		for (OrderedConfigurationImpl.Entry entry : entries) {
			entryMap.put(entry.getContributionId(), entry);
		}
		List<Object> values = new ArrayList<>(entryMap.size());
		for (OrderedConfigurationImpl.Entry entry : entryMap.values()) {
			values.add(entry.getValueBuilder().build(context));
		}
		return Collections.unmodifiableList(values);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<Object, Object> buildMappedContributions(ServiceBuilderContext context) {
		if (dependencies.mappedContributions == null) {
			return Collections.emptyMap();
		}
		MappedConfigurationImpl configuration = new MappedConfigurationImpl();
		for (MappedContributor contributor : dependencies.mappedContributions) {
			contributor.contribute(context, configuration);
		}
		List<MappedConfigurationImpl.Entry> entries = configuration.getEntries();
		Map<String, MappedConfigurationImpl.Entry> entryMap = new LinkedHashMap<>();
		for (MappedConfigurationImpl.Entry entry : entries) {
			entryMap.put(entry.getContributionId(), entry);
		}
		Map<Object, Object> values = new LinkedHashMap<>();
		for (MappedConfigurationImpl.Entry entry : entryMap.values()) {
			values.put(entry.getKeyBuilder().build(context), entry.getValueBuilder().build(context));
		}
		return Collections.unmodifiableMap(values);
	}

	public String getServiceId() {
		return serviceId;
	}
}