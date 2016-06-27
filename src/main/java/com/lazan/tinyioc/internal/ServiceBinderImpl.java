package com.lazan.tinyioc.internal;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBindOptions;
import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceDecorator;

public class ServiceBinderImpl implements ServiceBinder {
	private List<ServiceBindOptionsImpl> bindList = new LinkedList<>();
	private List<ServiceBindOptionsImpl> overrideList = new LinkedList<>();
	private Map<String, ServiceDecorator<?>> decoratorsByServiceId = new LinkedHashMap<>(); 
	private Map<Class<?>, ServiceDecorator<?>> decoratorsByServiceType = new LinkedHashMap<>(); 
	
	@Override
	public <T> ServiceBindOptions bind(Class<T> serviceType) {
		return bind(serviceType, new ConstructorServiceBuilder<>(serviceType));
	}
	
	@Override
	public <T> ServiceBindOptions bind(Class<T> serviceType, T service) {
		return bind(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T, C extends T> ServiceBindOptions bind(Class<T> serviceType, Class<C> concreteType) {
		return bind(serviceType, new ConstructorServiceBuilder<C>(concreteType));
	}
	
	@Override
	public <T, C extends T> ServiceBindOptions bind(Class<T> serviceType, ServiceBuilder<C> builder) {
		ServiceBindOptionsImpl bindOptions = new ServiceBindOptionsImpl(serviceType, builder);
		bindList.add(bindOptions);
		return bindOptions;
	}
	
	@Override
	public <T, C extends T> ServiceBindOptions override(Class<T> serviceType, Class<C> concreteType) {
		return override(serviceType, new ConstructorServiceBuilder<C>(concreteType));
	}
	
	@Override
	public <T> ServiceBindOptions override(Class<T> serviceType, T service) {
		return override(serviceType, new ConstantServiceBuilder<T>(service));
	}
	
	@Override
	public <T, C extends T> ServiceBindOptions override(Class<T> serviceType, ServiceBuilder<C> builder) {
		ServiceBindOptionsImpl bindOptions = new ServiceBindOptionsImpl(serviceType, builder);
		overrideList.add(bindOptions);
		return bindOptions;
	}
	
	@Override
	public <T> void decorate(Class<T> serviceType, ServiceDecorator<T> decorator) {
		if (decoratorsByServiceType.containsKey(serviceType)) {
			throw new IocException("Multiple decorators found for serviceType '%s'", serviceType.getName());
		}
		decoratorsByServiceType.put(serviceType, decorator);
	}
	
	@Override
	public <T> void decorate(String serviceId, ServiceDecorator<T> decorator) {
		if (decoratorsByServiceId.containsKey(serviceId)) {
			throw new IocException("Multiple decorators found for serviceId '%s'", serviceId);
		}
		decoratorsByServiceId.put(serviceId, decorator);
	}
	
	public List<ServiceBindOptionsImpl> getBindList() {
		return bindList;
	}
	
	public List<ServiceBindOptionsImpl> getOverrideList() {
		return overrideList;
	}
	
	public Map<String, ServiceDecorator<?>> getDecoratorsByServiceId() {
		return decoratorsByServiceId;
	}
	
	public Map<Class<?>, ServiceDecorator<?>> getDecoratorsByServiceType() {
		return decoratorsByServiceType;
	}
}