package com.lazan.tinyioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;

public class ServiceBinderImpl implements ServiceBinder {
	private List<ServiceBinderOptionsImpl> bindList = new LinkedList<>();
	private List<ServiceBinderOptionsImpl> overrideList = new LinkedList<>();
	private List<ServiceDecoratorOptionsImpl> decoratorList = new LinkedList<>(); 
	
	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType) {
		return bind(serviceType, new ConstructorServiceBuilder<>(serviceType));
	}
	
	@Override
	public <T> ServiceBinderOptions bind(Class<T> serviceType, T service) {
		return bind(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T, C extends T> ServiceBinderOptions bind(Class<T> serviceType, Class<C> concreteType) {
		return bind(serviceType, new ConstructorServiceBuilder<C>(concreteType));
	}
	
	@Override
	public <T, C extends T> ServiceBinderOptions bind(Class<T> serviceType, ServiceBuilder<C> builder) {
		ServiceBinderOptionsImpl options = new ServiceBinderOptionsImpl(serviceType, builder);
		bindList.add(options);
		return options;
	}
	
	@Override
	public <T, C extends T> ServiceBinderOptions override(Class<T> serviceType, Class<C> concreteType) {
		return override(serviceType, new ConstructorServiceBuilder<C>(concreteType));
	}
	
	@Override
	public <T> ServiceBinderOptions override(Class<T> serviceType, T service) {
		return override(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T, C extends T> ServiceBinderOptions override(Class<T> serviceType, ServiceBuilder<C> builder) {
		ServiceBinderOptionsImpl options = new ServiceBinderOptionsImpl(serviceType, builder);
		overrideList.add(options);
		return options;
	}
	
	@Override
	public <T> ServiceDecoratorOptions decorate(Class<T> serviceType, ServiceDecorator<T> decorator) {
		ServiceDecoratorOptionsImpl options = new ServiceDecoratorOptionsImpl(serviceType, decorator);
		decoratorList.add(options);
		return options;
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
}