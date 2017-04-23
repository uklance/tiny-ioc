package com.lazan.tinyioc.internal;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.MappedContributor;
import com.lazan.tinyioc.OrderedContributor;
import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;
import com.lazan.tinyioc.UnorderedContributor;

public class ServiceBinderImpl implements ServiceBinder {
	private List<ServiceBinderOptionsImpl> bindList = new LinkedList<>();
	private List<ServiceBinderOptionsImpl> overrideList = new LinkedList<>();
	private List<ServiceDecoratorOptionsImpl> decoratorList = new LinkedList<>(); 
	private Map<String, List<OrderedContributor<?>>> orderedContributors = new LinkedHashMap<>();
	private Map<String, List<UnorderedContributor<?>>> unorderedContributors = new LinkedHashMap<>();
	private Map<String, List<MappedContributor<?, ?>>> mappedContributors = new LinkedHashMap<>();

	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType) {
		return bind(serviceType, new InjectionServiceBuilder<>(serviceType));
	}
	
	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType, T service) {
		return bind(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType, Class<? extends T> concreteType) {
		return bind(serviceType, new InjectionServiceBuilder<>(concreteType));
	}
	
	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType, ServiceBuilder<? extends T> builder) {
		ServiceBinderOptionsImpl options = new ServiceBinderOptionsImpl(serviceType, builder);
		bindList.add(options);
		return options;
	}
	
	@Override
	public <T> ServiceBinderOptions override(Class<T> serviceType, Class<? extends T> concreteType) {
		return override(serviceType, new InjectionServiceBuilder<>(concreteType));
	}
	
	@Override
	public <T> ServiceBinderOptions override(Class<T> serviceType, T service) {
		return override(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T> ServiceBinderOptions override(Class<T> serviceType, ServiceBuilder<? extends T> builder) {
		ServiceBinderOptionsImpl options = new ServiceBinderOptionsImpl(serviceType, builder);
		overrideList.add(options);
		return options;
	}
	
	@Override
	public <T> ServiceDecoratorOptions decorate(Class<T> serviceType, String decoratorId, ServiceDecorator<? extends T> decorator) {
		ServiceDecoratorOptionsImpl options = new ServiceDecoratorOptionsImpl(serviceType, decoratorId, decorator);
		decoratorList.add(options);
		return options;
	}
	
	@Override
	public void contribute(String serviceId, OrderedContributor<?> contributor) {
		List<OrderedContributor<?>> list = orderedContributors.get(serviceId);
		if (list == null) {
			list = new LinkedList<>();
			orderedContributors.put(serviceId,  list);
		}
		list.add(contributor);
	}

	@Override
	public void contribute(Class<?> serviceType, OrderedContributor<?> contributor) {
		contribute(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributor);
	}

	@Override
	public void contribute(String serviceId, UnorderedContributor<?> contributor) {
		List<UnorderedContributor<?>> list = unorderedContributors.get(serviceId);
		if (list == null) {
			list = new LinkedList<>();
			unorderedContributors.put(serviceId,  list);
		}
		list.add(contributor);
	}

	@Override
	public void contribute(Class<?> serviceType, UnorderedContributor<?> contributor) {
		contribute(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributor);
	}

	@Override
	public void contribute(String serviceId, MappedContributor<?, ?> contributor) {
		List<MappedContributor<?, ?>> list = mappedContributors.get(serviceId);
		if (list == null) {
			list = new LinkedList<>();
			mappedContributors.put(serviceId,  list);
		}
		list.add(contributor);
	}

	@Override
	public void contribute(Class<?> serviceType, MappedContributor<?, ?> contributor) {
		contribute(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributor);
	}

	public List<ServiceBinderOptionsImpl> getBindList() {
		return bindList;
	}
	
	public List<ServiceBinderOptionsImpl> getOverrideList() {
		return overrideList;
	}
	
	public List<ServiceDecoratorOptionsImpl> getDecoratorList() {
		return decoratorList;
	}
	
	public Map<String, List<OrderedContributor<?>>> getOrderedContributors() {
		return orderedContributors;
	}
	
	public Map<String, List<UnorderedContributor<?>>> getUnorderedContributors() {
		return unorderedContributors;
	}
	
	public Map<String, List<MappedContributor<?, ?>>> getMappedContributors() {
		return mappedContributors;
	}
}