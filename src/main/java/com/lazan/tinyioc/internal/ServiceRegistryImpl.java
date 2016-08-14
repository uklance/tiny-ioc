package com.lazan.tinyioc.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceModule;
import com.lazan.tinyioc.ServiceRegistry;

public class ServiceRegistryImpl implements ServiceRegistry {
	private final Set<String> idStack;
	private final Map<String, ServiceReference<?>> referencesById;
	private final Map<Class<?>, List<ServiceReference<?>>> referencesByType;
	
	public ServiceRegistryImpl(Iterable<ServiceModule> modules) {
		Map<String, ServiceReference<?>> _referencesById = new LinkedHashMap<>();
		Map<Class<?>, List<ServiceReference<?>>> _referencesByType = new LinkedHashMap<>();
		
		ServiceBinderImpl binder = new ServiceBinderImpl();
		
		for (ServiceModule module : modules) {
			module.bind(binder);
		}
		
		Map<String, ServiceBinderOptionsImpl> overrideMap = createOverrideMap(binder);
		Map<String, ServiceDecoratorOptionsImpl> decoratorMap = createDecoratorMap(binder);	
		
		for (ServiceBinderOptionsImpl candidate : binder.getBindList()) {
			String serviceId = getServiceId(candidate);
			Class<?> serviceType = candidate.getServiceType();
			if (_referencesById.containsKey(serviceId)) {
				throw new IocException("Duplicate serviceId '%s'", serviceId);
			}
			ServiceBuilder<?> overrideBuilder = getOverrideServiceBuilder(serviceId, serviceType, overrideMap);
			ServiceBuilder<?> builder = overrideBuilder == null ? candidate.getServiceBuilder() : overrideBuilder;
			ServiceDecorator<?> decorator = getServiceDecorator(serviceId, serviceType, decoratorMap);

			@SuppressWarnings({"unchecked", "rawtypes"})
			ServiceReference<?> reference = new ServiceReference(serviceId, serviceType, builder, decorator);
			_referencesById.put(serviceId, reference);

			List<ServiceReference<?>> referenceList = _referencesByType.get(serviceType);
			if (referenceList == null) {
				referenceList = new LinkedList<>();
				_referencesByType.put(serviceType, referenceList);
			}
			referenceList.add(reference);
		}
		
		for (String serviceId : overrideMap.keySet()) {
			if (!_referencesById.containsKey(serviceId)) {
				throw new IocException("Attempted to override unknown serviceId '%s'", serviceId);
			}
		}
		for (String serviceId : decoratorMap.keySet()) {
			if (!_referencesById.containsKey(serviceId)) {
				throw new IocException("Attempted to decorate unknown serviceId '%s'", serviceId);
			}
		}

		idStack = Collections.emptySet();
		referencesById = Collections.unmodifiableMap(_referencesById);
		referencesByType = Collections.unmodifiableMap(_referencesByType);
	}
	
	protected ServiceRegistryImpl(ServiceRegistryImpl registry, String serviceId) {
		this.referencesById = registry.referencesById;
		this.referencesByType = registry.referencesByType;
		
		Set<String> _idStack = new LinkedHashSet<>(registry.idStack);
		_idStack.add(serviceId);
		this.idStack = Collections.unmodifiableSet(_idStack);
	}	

	protected ServiceDecorator<?> getServiceDecorator(
			String serviceId,
			Class<?> serviceType, Map<String, 
			ServiceDecoratorOptionsImpl> decoratorMap)
	{
		ServiceDecoratorOptionsImpl options = decoratorMap.get(serviceId);
		if (options == null) {
			return null;
		}
		if (!options.getServiceType().equals(serviceType)) {
			throw new IocException("Invalid decorator for serviceId '%s', expected %s found %s",
					serviceId, serviceType.getName(), options.getServiceType().getName());
		}
		return options.getServiceDecorator();
	}

	protected ServiceBuilder<?> getOverrideServiceBuilder(
			String serviceId,
			Class<?> serviceType, 
			Map<String, ServiceBinderOptionsImpl> overrideMap) {
		ServiceBinderOptionsImpl options = overrideMap.get(serviceId);
		if (options == null) {
			return null;
		}
		if (!options.getServiceType().equals(serviceType)) {
			throw new IocException("Invalid override for serviceId '%s', expected %s found %s",
					serviceId, serviceType.getName(), options.getServiceType().getName());
		}
		return options.getServiceBuilder();
	}

	protected Map<String, ServiceDecoratorOptionsImpl> createDecoratorMap(ServiceBinderImpl binder) {
		Map<String, ServiceDecoratorOptionsImpl> decoratorMap = new LinkedHashMap<>();
		for (ServiceDecoratorOptionsImpl decorateOptions : binder.getDecoratorList()) {
			String serviceId = getServiceId(decorateOptions);
			if (decoratorMap.containsKey(serviceId)) {
				throw new IocException("Duplicate decorator for serviceId '%s'", serviceId);
			}
			decoratorMap.put(serviceId, decorateOptions);
		}
		return decoratorMap;
	}

	protected Map<String, ServiceBinderOptionsImpl> createOverrideMap(ServiceBinderImpl binder) {
		Map<String, ServiceBinderOptionsImpl> overrideMap = new LinkedHashMap<>();
		for (ServiceBinderOptionsImpl overrideOptions : binder.getOverrideList()) {
			String serviceId = getServiceId(overrideOptions);
			if (overrideMap.containsKey(serviceId)) {
				throw new IocException("Duplicate override for serviceId '%s'", serviceId);
			}
			overrideMap.put(serviceId, overrideOptions);
		}
		return overrideMap;
	}

	@Override
	public <T> T getService(Class<T> serviceType) {
		List<ServiceReference<?>> references = referencesByType.get(serviceType);
		int count = references == null ? 0 : references.size();
		if (count != 1) {
			throw new IocException("Found %s services for serviceType '%s', expecting 1", count, serviceType.getName());
		}
		ServiceReference<?> reference = references.get(0);
		return serviceType.cast(reference.get(this));
	}
	
	@Override
	public Object getService(String serviceId) {
		ServiceReference<?> reference = referencesById.get(serviceId);
		if (reference == null) {
			throw new IocException("No service found for serviceId '%s'", serviceId);
		}
		return reference.get(this);
	}
	
	@Override
	public <T> T getService(String serviceId, Class<T> serviceType) {
		try {
			return serviceType.cast(getService(serviceId));
		} catch (ClassCastException e) {
			throw new IocException(e, "Incompatible type for serviceId '%s'", serviceId);
		}
	}
	
	@Override
	public <T> Map<String, T> getServices(Class<T> serviceType) {
		Map<String, T> services = new LinkedHashMap<>();
		List<ServiceReference<?>> references = referencesByType.get(serviceType);
		for (ServiceReference<?> reference : references) {
			T service = serviceType.cast(reference.get(this));
			services.put(reference.getServiceId(), service);
		}
		return Collections.unmodifiableMap(services);
	}
	
	@Override
	public Set<String> getServiceIds() {
		return referencesById.keySet();
	}
	
	@Override
	public Set<Class<?>> getServiceTypes() {
		return referencesByType.keySet();
	}
	
	public Set<String> getServiceIdStack() {
		return idStack;
	}

	protected String getServiceId(ServiceBinderOptionsImpl options) {
		if (options.getServiceId() != null) {
			return options.getServiceId();
		}
		return getDefaultServiceId(options.getServiceType());
	}
	
	protected String getServiceId(ServiceDecoratorOptionsImpl options) {
		if (options.getServiceId() != null) {
			return options.getServiceId();
		}
		return getDefaultServiceId(options.getServiceType());
	}
	
	static String getDefaultServiceId(Class<?> serviceType) {
		String simpleName = serviceType.getSimpleName();
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
	}
}
