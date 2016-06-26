package com.lazan.simpleioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.simpleioc.ServiceBindOptions;
import com.lazan.simpleioc.ServiceBinder;
import com.lazan.simpleioc.ServiceBuilder;

public class ServiceBinderImpl implements ServiceBinder {
	private List<ServiceBindOptionsImpl> bindList = new LinkedList<>();
	private List<ServiceBindOptionsImpl> overrideList = new LinkedList<>();
	
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
	
	public List<ServiceBindOptionsImpl> getBindList() {
		return bindList;
	}
	
	public List<ServiceBindOptionsImpl> getOverrideList() {
		return overrideList;
	}
}