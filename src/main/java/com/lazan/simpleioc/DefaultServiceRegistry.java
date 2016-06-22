package com.lazan.simpleioc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.simpleioc.internal.ConstructorServiceBuilder;

public class DefaultServiceRegistry implements ServiceRegistry {
	private final Set<String> serviceIdStack;
	private final Map<String, ServicePointer> pointersByServiceId;
	private final Map<Class<?>, List<ServicePointer>> pointersByServiceType;
	
	public DefaultServiceRegistry(ServiceModule... modules) {
		Map<String, ServicePointer> _pointersByServiceId = new LinkedHashMap<>();
		Map<Class<?>, List<ServicePointer>> _pointersByServiceType = new LinkedHashMap<>();
		
		DefaultServiceBinder binder = new DefaultServiceBinder();
		
		for (ServiceModule module : modules) {
			module.bind(binder);
		}
		
		Map<String, DefaultServiceBindOptions> overrideMap = new LinkedHashMap<>();
		for (DefaultServiceBindOptions override : binder.getOverrideList()) {
			String serviceId = getServiceId(override);
			if (overrideMap.containsKey(serviceId)) {
				throw new IocException("Duplicate override for serviceId '%s'", serviceId);
			}
			overrideMap.put(serviceId, override);
		}
		for (DefaultServiceBindOptions candidate : binder.getBindList()) {
			String serviceId = getServiceId(candidate);
			Class<?> serviceType = candidate.getServiceType();
			if (_pointersByServiceId.containsKey(serviceId)) {
				throw new IocException("Duplicate serviceId '%s'", serviceId);
			}
			DefaultServiceBindOptions override = overrideMap.get(serviceId);
			
			DefaultServiceBindOptions bindOptions;
			if (override == null) {
				bindOptions = candidate;
			} else {
				if (!override.getServiceType().equals(serviceType)) {
					throw new IocException("Invalid override for servieId '%s', expected %s found %s",
							serviceId, serviceType.getName(), override.getServiceType().getName());
				}
				bindOptions = override;
			}
			ServicePointer servicePointer = new ServicePointer(serviceId, bindOptions.getServiceBuilder());
			
			_pointersByServiceId.put(serviceId, servicePointer);
			List<ServicePointer> pointerList = _pointersByServiceType.get(serviceType);
			if (pointerList == null) {
				pointerList = new LinkedList<>();
				_pointersByServiceType.put(serviceType, pointerList);
			}
			pointerList.add(servicePointer);
		}
		
		serviceIdStack = Collections.emptySet();
		pointersByServiceId = Collections.unmodifiableMap(_pointersByServiceId);
		pointersByServiceType = Collections.unmodifiableMap(_pointersByServiceType);
	}

	protected DefaultServiceRegistry(DefaultServiceRegistry registry, String serviceId) {
		this.pointersByServiceId = registry.pointersByServiceId;
		this.pointersByServiceType = registry.pointersByServiceType;
		this.serviceIdStack = new LinkedHashSet<>(registry.serviceIdStack);
		this.serviceIdStack.add(serviceId);
	}

	protected String getServiceId(DefaultServiceBindOptions bindOptions) {
		if (bindOptions.getServiceId() != null) {
			return bindOptions.getServiceId();
		}
		String simpleName = bindOptions.getServiceType().getSimpleName();
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
	}
	
	@Override
	public <T> T getService(Class<T> serviceType) {
		List<ServicePointer> pointers = pointersByServiceType.get(serviceType);
		int count = pointers == null ? 0 : pointers.size();
		if (count != 1) {
			throw new IocException("Found %s services for serviceType %s. expecting 1", count, serviceType.getName());
		}
		ServicePointer pointer = pointers.get(0);
		return serviceType.cast(pointer.get(this));
	}
	
	@Override
	public Object getService(String serviceId) {
		ServicePointer pointer = pointersByServiceId.get(serviceId);
		if (pointer == null) {
			throw new IocException("No service found for serviceId '%s'", serviceId);
		}
		return pointer.get(this);
	}
	
	@Override
	public <T> T getService(String serviceId, Class<T> serviceType) {
		try {
			return serviceType.cast(getService(serviceId));
		} catch (ClassCastException e) {
			throw new IocException("Incompatible type for serviceId '%s'", serviceId);
		}
	}
	
	@Override
	public Set<String> getServiceIds() {
		return pointersByServiceId.keySet();
	}
	
	@Override
	public Set<Class<?>> getServiceTypes() {
		return pointersByServiceType.keySet();
	}

	public static class DefaultServiceBinder implements ServiceBinder {
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
	
	public static class DefaultServiceBindOptions implements ServiceBindOptions {
		private final Class<?> serviceType;
		private final ServiceBuilder<?> builder;
		private String serviceId;
		
		public DefaultServiceBindOptions(Class<?> serviceType, ServiceBuilder<?> builder) {
			super();
			this.serviceType = serviceType;
			this.builder = builder;
		}

		@Override
		public ServiceBindOptions withServiceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}
		
		public String getServiceId() {
			return serviceId;
		}
		
		public Class<?> getServiceType() {
			return serviceType;
		}
		
		public ServiceBuilder<?> getServiceBuilder() {
			return builder;
		}
	}
	
	public static class ConstantServiceBuilder<T> implements ServiceBuilder<T> {
		private final T service;
		
		public ConstantServiceBuilder(T service) {
			super();
			this.service = service;
		}

		@Override
		public T build(ServiceBuilderContext context) {
			return service;
		}
	}
	
	public class ServicePointer {
		private final String serviceId;
		private final ServiceBuilder<?> builder;
		private volatile Object service;
		
		public ServicePointer(String serviceId, ServiceBuilder<?> builder) {
			super();
			this.serviceId = serviceId;
			this.builder = builder;
		}

		public synchronized Object get(DefaultServiceRegistry registry) {
			if (service == null) {
				if (registry.serviceIdStack.contains(serviceId)) {
					List<String> references = new LinkedList<>(registry.serviceIdStack);
					references.add(serviceId);
					throw new IocException("Circular dependency reference detected %s", references);
				}
				DefaultServiceRegistry registryWrapper = new DefaultServiceRegistry(registry, serviceId);
				ServiceBuilderContext context = new DefaultServiceBuilderContext(serviceId, registryWrapper);
				service = builder.build(context);
			}
			return service;
		}
	}
	
	public class DefaultServiceBuilderContext implements ServiceBuilderContext {
		private final String serviceId;
		private final ServiceRegistry registry;
		public DefaultServiceBuilderContext(String serviceId, ServiceRegistry registry) {
			super();
			this.serviceId = serviceId;
			this.registry = registry;
		}
		public String getServiceId() {
			return serviceId;
		}
		public ServiceRegistry getServiceRegistry() {
			return registry;
		}
	}
}
