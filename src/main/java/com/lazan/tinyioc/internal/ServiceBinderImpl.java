package com.lazan.tinyioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.tinyioc.OrderedContributionOptions;
import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;

public class ServiceBinderImpl implements ServiceBinder {
	private List<ServiceBinderOptionsImpl> bindList = new LinkedList<>();
	private List<ServiceBinderOptionsImpl> overrideList = new LinkedList<>();
	private List<ServiceDecoratorOptionsImpl> decoratorList = new LinkedList<>(); 
	private List<UnorderedContributionOptionsImpl> unorderedContributions = new LinkedList<>();
	private List<OrderedContributionOptionsImpl> orderedContributions = new LinkedList<>();
	private List<MappedContributionOptionsImpl> mappedContributions = new LinkedList<>();
	
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
	public void mappedContribution(String serviceId, String contributionId, Object key, Object value) {
		ServiceBuilder<?> keyBuilder = new ConstantServiceBuilder<>(key);
		ServiceBuilder<?> valueBuilder = new ConstantServiceBuilder<>(value);
		mappedContribution(serviceId, contributionId, keyBuilder, valueBuilder);
	}
	
	@Override
	public void mappedContribution(String serviceId, String contributionId, Class<?> keyType, Class<?> valueType) {
		ServiceBuilder<?> keyBuilder = new InjectionServiceBuilder<>(keyType);
		ServiceBuilder<?> valueBuilder = new ConstantServiceBuilder<>(valueType);
		mappedContribution(serviceId, contributionId, keyBuilder, valueBuilder);
	}
	
	@Override
	public void mappedContribution(String serviceId, String contributionId, ServiceBuilder<?> keyBuilder, ServiceBuilder<?> valueBuilder) {
		MappedContributionOptionsImpl options = new MappedContributionOptionsImpl(serviceId, contributionId, keyBuilder, valueBuilder);
		mappedContributions.add(options);
	}
	
	@Override
	public void mappedContribution(Class<?> serviceType, String contributionId, Object key, Object value) {
		mappedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, key, value);
	}
	
	@Override
	public void mappedContribution(Class<?> serviceType, String contributionId, Class<?> keyType, Class<?> valueType) {
		mappedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, keyType, valueType);
	}
	
	@Override
	public void mappedContribution(Class<?> serviceType, String contributionId, ServiceBuilder<?> keyBuilder, ServiceBuilder<?> valueBuilder) {
		mappedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, keyBuilder, valueBuilder);
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(String serviceId, String contributionId, Object value) {
		return orderedContribution(serviceId, contributionId, new ConstantServiceBuilder<>(value));
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(String serviceId, String contributionId, Class<?> type) {
		return orderedContribution(serviceId, contributionId, new InjectionServiceBuilder<>(type));
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(String serviceId, String contributionId, ServiceBuilder<?> builder) {
		OrderedContributionOptionsImpl options = new OrderedContributionOptionsImpl(serviceId, contributionId, builder);
		orderedContributions.add(options);
		return options;
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(Class<?> serviceType, String contributionId, Object value) {
		return orderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, value);
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(Class<?> serviceType, String contributionId, Class<?> type) {
		return orderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, type);
	}
	
	@Override
	public OrderedContributionOptions orderedContribution(Class<?> serviceType, String contributionId, ServiceBuilder<?> builder) {
		return orderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, builder);
	}
	
	@Override
	public void unorderedContribution(String serviceId, String contributionId, ServiceBuilder<?> builder) {
		UnorderedContributionOptionsImpl options = new UnorderedContributionOptionsImpl(serviceId, contributionId, builder);
		unorderedContributions.add(options);
	}
	
	@Override
	public void unorderedContribution(String serviceId, String contributionId, Object value) {
		unorderedContribution(serviceId, contributionId, new ConstantServiceBuilder<>(value));
	}
	
	@Override
	public void unorderedContribution(String serviceId, String contributionId, Class<?> type) {
		unorderedContribution(serviceId, contributionId, new InjectionServiceBuilder<>(type));
	}
	
	@Override
	public void unorderedContribution(Class<?> serviceType, String contributionId, Object value) {
		unorderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, value);
	}
	
	@Override
	public void unorderedContribution(Class<?> serviceType, String contributionId, Class<?> type) {
		unorderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, type);
	}
	
	@Override
	public void unorderedContribution(Class<?> serviceType, String contributionId, ServiceBuilder<?> builder) {
		unorderedContribution(ServiceRegistryImpl.getDefaultServiceId(serviceType), contributionId, builder);
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
	
	public List<OrderedContributionOptionsImpl> getOrderedContributions() {
		return orderedContributions;
	}
	
	public List<UnorderedContributionOptionsImpl> getUnorderedContributions() {
		return unorderedContributions;
	}
	
	public List<MappedContributionOptionsImpl> getMappedContributions() {
		return mappedContributions;
	}
}