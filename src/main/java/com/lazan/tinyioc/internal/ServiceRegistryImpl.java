package com.lazan.tinyioc.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lazan.tinyioc.IocException;
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
		Map<String, List<ServiceDecoratorOptionsImpl>> decoratorMap = createDecoratorMap(binder);
		
		Map<String, List<UnorderedContributionOptionsImpl>> unorderedContributionMap = groupByServiceId(binder.getUnorderedContributions());
		Map<String, List<OrderedContributionOptionsImpl>> orderedContributionMap = groupByServiceId(binder.getOrderedContributions());
		Map<String, List<MappedContributionOptionsImpl>> mappedContributionMap = groupByServiceId(binder.getMappedContributions());
		
		for (ServiceBinderOptionsImpl candidate : binder.getBindList()) {
			String serviceId = getServiceId(candidate);
			Class<?> serviceType = candidate.getServiceType();
			if (_referencesById.containsKey(serviceId)) {
				throw new IocException("Duplicate serviceId '%s'", serviceId);
			}
			ServiceBinderOptionsImpl override = overrideMap.get(serviceId);
			if (override != null) {
				if (!override.getServiceType().equals(candidate.getServiceType())) {
					throw new IocException("Invalid override for serviceId '%s' (expected serviceType %s, found %s)", 
							serviceId, candidate.getServiceType().getName(), override.getServiceType().getName());
				}
				if (!override.getContributionType().equals(candidate.getContributionType())) {
					throw new IocException("Invalid override for serviceId '%s' (expected contributionType %s, found %s)", 
							serviceId, candidate.getContributionType(), override.getContributionType());
				}
				if (!isEqual(override.getContributionKeyType(), candidate.getContributionKeyType())) {
					throw new IocException("Invalid override for serviceId '%s' (expected contributionKeyType %s, found %s)", 
							serviceId, candidate.getContributionKeyType().getName(), override.getContributionKeyType().getName());
				}
				if (!isEqual(override.getContributionValueType(), candidate.getContributionValueType())) {
					throw new IocException("Invalid override for serviceId '%s' (expected contributionValueType %s, found %s)", 
							serviceId, candidate.getContributionValueType().getName(), override.getContributionValueType().getName());
				}
			}
			ServiceBinderOptionsImpl options = override == null ? candidate : override;
			List<ServiceDecorator<?>> decorators = buildServiceDecorators(serviceId, serviceType, decoratorMap);
			List<UnorderedContributionOptionsImpl> unorderedContributions = unorderedContributionMap.get(serviceId);
			List<OrderedContributionOptionsImpl> orderedContributions = orderedContributionMap.get(serviceId);
			List<MappedContributionOptionsImpl> mappedContributions = mappedContributionMap.get(serviceId);

			@SuppressWarnings({"unchecked", "rawtypes"})
			ServiceReference<?> reference = new ServiceReference(
					serviceId, serviceType, options.getServiceBuilder(), options.isEagerLoad(), decorators, 
					options.getContributionType(), options.getContributionKeyType(), options.getContributionValueType(), 
					unorderedContributions, orderedContributions, mappedContributions);
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

		for (ServiceReference<?> reference : referencesById.values()) {
			reference.init(this);
		}
	}
	
	private boolean isEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else if (o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}
	
	private <T extends UnorderedContributionOptionsImpl> Map<String, List<T>> groupByServiceId(List<T> contributions) {
		Map<String, List<T>> grouped = new LinkedHashMap<>();
		for (T contribution : contributions) {
			List<T> list = grouped.get(contribution.getServiceId());
			if (list == null) {
				list = new LinkedList<>();
				grouped.put(contribution.getServiceId(), list);
			}
			list.add(contribution);
		}
		return grouped;
	}

	protected ServiceRegistryImpl(ServiceRegistryImpl registry, String serviceId) {
		this.referencesById = registry.referencesById;
		this.referencesByType = registry.referencesByType;
		
		Set<String> _idStack = new LinkedHashSet<>(registry.idStack);
		_idStack.add(serviceId);
		this.idStack = Collections.unmodifiableSet(_idStack);
	}	

	protected List<ServiceDecorator<?>> buildServiceDecorators(
			String serviceId,
			Class<?> serviceType, 
			Map<String, List<ServiceDecoratorOptionsImpl>> decoratorMap)
	{
		List<ServiceDecoratorOptionsImpl> optionsList = decoratorMap.get(serviceId);
		if (optionsList == null) {
			return null;
		}
		List<ServiceDecorator<?>> decorators = new LinkedList<>();
		for (ServiceDecoratorOptionsImpl options : optionsList) {
			if (!options.getServiceType().equals(serviceType)) {
				throw new IocException("Invalid decorator '%s' for serviceId '%s', expected %s found %s",
						options.getDecoratorId(), serviceId, serviceType.getName(), options.getServiceType().getName());
			}
			decorators.add(options.getServiceDecorator());
		}
		return decorators;
	}

	protected Map<String, List<ServiceDecoratorOptionsImpl>> createDecoratorMap(ServiceBinderImpl binder) {
		Map<String, List<ServiceDecoratorOptionsImpl>> decoratorMap = new LinkedHashMap<>();
		for (ServiceDecoratorOptionsImpl decorateOptions : binder.getDecoratorList()) {
			String serviceId = getServiceId(decorateOptions);
			List<ServiceDecoratorOptionsImpl> list = decoratorMap.get(serviceId);
			if (list == null) {
				list = new LinkedList<>();
				decoratorMap.put(serviceId, list);
			}
			list.add(decorateOptions);
		}
		for (Map.Entry<String, List<ServiceDecoratorOptionsImpl>> entry : decoratorMap.entrySet()) {
			String serviceId = entry.getKey();
			List<ServiceDecoratorOptionsImpl> list = entry.getValue();
			Set<String> decoratorIds = new LinkedHashSet<>();
			for (ServiceDecoratorOptionsImpl options : list) {
				if (!decoratorIds.add(options.getDecoratorId())) {
					throw new IocException("Duplicate decoratorId '%s' for serviceId '%s'", options.getDecoratorId(), serviceId);
				}
			}
			Collections.sort(list);
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
