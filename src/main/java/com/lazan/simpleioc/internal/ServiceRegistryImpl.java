package com.lazan.simpleioc.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.simpleioc.IocException;
import com.lazan.simpleioc.ServiceBuilderContext;
import com.lazan.simpleioc.ServiceDecorator;
import com.lazan.simpleioc.ServiceModule;
import com.lazan.simpleioc.ServiceRegistry;

public class ServiceRegistryImpl implements ServiceRegistry {
	private final Set<String> serviceIdStack;
	private final Map<String, ServicePointer> pointersByServiceId;
	private final Map<Class<?>, List<ServicePointer>> pointersByServiceType;
	
	public ServiceRegistryImpl(Iterable<ServiceModule> modules) {
		Map<String, ServicePointer> _pointersByServiceId = new LinkedHashMap<>();
		Map<Class<?>, List<ServicePointer>> _pointersByServiceType = new LinkedHashMap<>();
		
		ServiceBinderImpl binder = new ServiceBinderImpl();
		
		for (ServiceModule module : modules) {
			module.bind(binder);
		}
		
		Map<String, ServiceBindOptionsImpl> overrideMap = new LinkedHashMap<>();
		for (ServiceBindOptionsImpl override : binder.getOverrideList()) {
			String serviceId = getServiceId(override);
			if (overrideMap.containsKey(serviceId)) {
				throw new IocException("Duplicate override for serviceId '%s'", serviceId);
			}
			overrideMap.put(serviceId, override);
		}
		for (ServiceBindOptionsImpl candidate : binder.getBindList()) {
			String serviceId = getServiceId(candidate);
			Class<?> serviceType = candidate.getServiceType();
			if (_pointersByServiceId.containsKey(serviceId)) {
				throw new IocException("Duplicate serviceId '%s'", serviceId);
			}
			ServiceBindOptionsImpl override = overrideMap.get(serviceId);
			
			ServiceBindOptionsImpl bindOptions;
			if (override == null) {
				bindOptions = candidate;
			} else {
				if (!override.getServiceType().equals(serviceType)) {
					throw new IocException("Invalid override for serviceId '%s', expected %s found %s",
							serviceId, serviceType.getName(), override.getServiceType().getName());
				}
				bindOptions = override;
			}
			
			ServiceDecorator<?> decorator1 = binder.getDecoratorsByServiceId().get(serviceId);
			ServiceDecorator<?> decorator2 = binder.getDecoratorsByServiceType().get(serviceType);
			if (decorator1 != null && decorator2 != null) {
				throw new IocException("Conflicting decorators registered for serviceId '%s' and serviceType '%s'",
						serviceId, serviceType.getName());
			}
			ServiceDecorator<?> decorator = decorator1 == null ? decorator2 : decorator1;
			
			ServicePointer servicePointer = new ServicePointer(serviceId, serviceType, bindOptions.getServiceBuilder(), decorator);
			_pointersByServiceId.put(serviceId, servicePointer);

			List<ServicePointer> pointerList = _pointersByServiceType.get(serviceType);
			if (pointerList == null) {
				pointerList = new LinkedList<>();
				_pointersByServiceType.put(serviceType, pointerList);
			}
			pointerList.add(servicePointer);
		}
		
		for (String serviceId : overrideMap.keySet()) {
			if (!_pointersByServiceId.containsKey(serviceId)) {
				throw new IocException("Attempted to override unknown serviceId '%s'", serviceId);
			}
		}
		for (String serviceId : binder.getDecoratorsByServiceId().keySet()) {
			if (!_pointersByServiceId.containsKey(serviceId)) {
				throw new IocException("Attempted to decorate unknown serviceId '%s'", serviceId);
			}
		}
		for (Class<?> serviceType : binder.getDecoratorsByServiceType().keySet()) {
			if (!_pointersByServiceType.containsKey(serviceType)) {
				throw new IocException("Attempted to decorate unknown serviceType '%s'", serviceType.getName());
			}
		}
		
		serviceIdStack = Collections.emptySet();
		pointersByServiceId = Collections.unmodifiableMap(_pointersByServiceId);
		pointersByServiceType = Collections.unmodifiableMap(_pointersByServiceType);
	}

	protected ServiceRegistryImpl(ServiceRegistryImpl registry, String serviceId) {
		this.pointersByServiceId = registry.pointersByServiceId;
		this.pointersByServiceType = registry.pointersByServiceType;
		
		Set<String> _serviceIdStack = new LinkedHashSet<>(registry.serviceIdStack);
		_serviceIdStack.add(serviceId);
		this.serviceIdStack = Collections.unmodifiableSet(_serviceIdStack);
	}

	@Override
	public <T> T getService(Class<T> serviceType) {
		List<ServicePointer> pointers = pointersByServiceType.get(serviceType);
		int count = pointers == null ? 0 : pointers.size();
		if (count != 1) {
			throw new IocException("Found %s services for serviceType '%s', expecting 1", count, serviceType.getName());
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
	public <T> Map<String, T> getServices(Class<T> serviceType) {
		Map<String, T> services = new LinkedHashMap<>();
		List<ServicePointer> pointers = pointersByServiceType.get(serviceType);
		for (ServicePointer pointer : pointers) {
			T service = serviceType.cast(pointer.get(this));
			services.put(pointer.getServiceId(), service);
		}
		return Collections.unmodifiableMap(services);
	}
	
	@Override
	public Set<String> getServiceIds() {
		return pointersByServiceId.keySet();
	}
	
	@Override
	public Set<Class<?>> getServiceTypes() {
		return pointersByServiceType.keySet();
	}
	
	public Set<String> getServiceIdStack() {
		return serviceIdStack;
	}

	protected String getServiceId(ServiceBindOptionsImpl bindOptions) {
		if (bindOptions.getServiceId() != null) {
			return bindOptions.getServiceId();
		}
		String simpleName = bindOptions.getServiceType().getSimpleName();
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
	}
	
	protected static class ServiceBuilderContextImpl implements ServiceBuilderContext {
		private final ServiceRegistry registry;
		private final String serviceId;
		private final Class<?> serviceType;
		
		public ServiceBuilderContextImpl(ServiceRegistry registry, String serviceId, Class<?> serviceType) {
			super();
			this.registry = registry;
			this.serviceId = serviceId;
			this.serviceType = serviceType;
		}

		@Override
		public String getServiceId() {
			return serviceId;
		}
		
		@Override
		public ServiceRegistry getServiceRegistry() {
			return registry;
		}
		
		@Override
		public Class<?> getServiceType() {
			return serviceType;
		}
	}
}
