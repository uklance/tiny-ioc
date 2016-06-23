package com.lazan.simpleioc.internal;

import java.util.LinkedList;
import java.util.List;

import com.lazan.simpleioc.ServiceBindOptions;
import com.lazan.simpleioc.ServiceBinder;
import com.lazan.simpleioc.ServiceBuilder;

public class DefaultServiceBinder implements ServiceBinder {
	private List<DefaultServiceBindOptions> bindList = new LinkedList<>();
	private List<DefaultServiceBindOptions> overrideList = new LinkedList<>();
	
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
		DefaultServiceBindOptions bindOptions = new DefaultServiceBindOptions(serviceType, builder);
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
		DefaultServiceBindOptions bindOptions = new DefaultServiceBindOptions(serviceType, builder);
		overrideList.add(bindOptions);
		return bindOptions;
	}
	
	public List<DefaultServiceBindOptions> getBindList() {
		return bindList;
	}
	
	public List<DefaultServiceBindOptions> getOverrideList() {
		return overrideList;
	}
}