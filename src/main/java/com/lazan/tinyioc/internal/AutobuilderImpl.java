package com.lazan.tinyioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.lazan.tinyioc.Autobuilder;
import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AutobuilderImpl implements Autobuilder {
	private static interface ContextValueSource<T> {
		T getValue(ServiceBuilderContext context);
	}
	
	private static final Map<Class<?>, ContextValueSource> CONTEXT_VALUE_SOURCES = new LinkedHashMap<>();
	static {
		CONTEXT_VALUE_SOURCES.put(Map.class, new ContextValueSource<Map>() {
			@Override
			public Map getValue(ServiceBuilderContext context) {
				return context.getMappedContributions();
			}
		});
		CONTEXT_VALUE_SOURCES.put(List.class, new ContextValueSource<List>() {
			@Override
			public List getValue(ServiceBuilderContext context) {
				return context.getOrderedContributions();
			}
		});
		CONTEXT_VALUE_SOURCES.put(Collection.class, new ContextValueSource<Collection>() {
			@Override
			public Collection getValue(ServiceBuilderContext context) {
				return context.getUnorderedContributions();
			}
		});
	}

	@Override
	public <T> T autobuild(ServiceRegistry registry, Class<T> concreteType) {
		return autobuild(registry, null, concreteType);
	}

	@Override
	public <T> T autobuild(ServiceBuilderContext context, Class<T> concreteType) {
		return autobuild(context.getServiceRegistry(), context, concreteType);
	}
	
	protected <T> T autobuild(ServiceRegistry registry, ServiceBuilderContext context, Class<T> concreteType) {
		try {
			Constructor<T> constructor = findConstructor(concreteType);
			Object[] params = getParameters(registry, context, constructor);
			T service = constructor.newInstance(params);
			injectFields(registry, context, service);
			return service;
		} catch (IocException e) {
			throw e;
		} catch (Exception e) {
			String msg;
			if (context != null) {
				msg = String.format("Error building serviceId '%s'", context.getServiceId());
			} else {
				msg = String.format("Error building service type '%s'", concreteType.getName());
			}
			throw new IocException(e, msg);
		}
	}
	
	protected Object[] getParameters(ServiceRegistry registry, ServiceBuilderContext context, Constructor<?> constructor) {
		Class[] paramTypes = constructor.getParameterTypes();
		if (paramTypes.length == 0) {
			return null;
		}
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; ++i) {
			Class<?> paramType = constructor.getParameterTypes()[i];
			Annotation[] annotations = constructor.getParameterAnnotations()[i];
			Named named = findAnnotation(annotations, Named.class);
			params[i] = getValue(registry, context, paramType, named);
		}
		return params;
	}

	protected Object getValue(ServiceRegistry registry, ServiceBuilderContext context, Class<?> paramType, Named named) {
		Object param;
		if (named != null) {
			param = registry.getService(named.value(), paramType);
		} else if (context != null && CONTEXT_VALUE_SOURCES.containsKey(paramType)) {
			param = CONTEXT_VALUE_SOURCES.get(paramType).getValue(context);
		} else {
			param = registry.getService(paramType);
		}
		return param;
	}

	protected <A extends Annotation> A findAnnotation(Annotation[] anns, Class<A> type) {
		for (Annotation ann : anns) {
			if (type.equals(ann.annotationType())) {
				return type.cast(ann);
			}
		}
		return null;
	}

	protected <T> Constructor<T> findConstructor(Class<T> concreteType) {
		Constructor[] constructors = concreteType.getConstructors();
		if (constructors.length == 0) {
			throw new IocException("No public constructors found for type %s", concreteType.getName());
		}
		if (constructors.length == 1) {
			return constructors[0];
		}
		int injectCount = 0;
		Constructor selected = null;
		for (Constructor current : constructors) {
			if (current.getAnnotation(Inject.class) != null) {
				selected = current;
				injectCount ++;
			}
		}
		if (injectCount == 1) {
			return selected;
		}
		if (injectCount == 0) {
			throw new IocException("Found %s public constructors for type %s, please annotate one with javax.inject.Inject", constructors.length, concreteType.getName());
		}
		throw new IocException("Found %s public constructors annotated with javax.inject.Inject for type %s", injectCount, concreteType.getName());
	}
	
	protected void injectFields(ServiceRegistry registry, ServiceBuilderContext context, Object service) {
		Class<?> currentType = service.getClass();
		do {
			for (Field field : currentType.getDeclaredFields()) {
				if (field.getAnnotation(Inject.class) != null) {
					Named named = field.getAnnotation(Named.class);
					try {
						Object value = getValue(registry, context, field.getType(), named);
						field.setAccessible(true);
						field.set(service, value);
					} catch (Exception e) {
						String msg;
						if (context != null) {
							msg = String.format("Error injecting field '%s' in serviceId '%s'", field.getName(), context.getServiceId());
						} else {
							msg = String.format("Error injecting field '%s' in type '%s'", field.getName(), service.getClass().getName());
						}
						throw new IocException(e, msg);
					}
				}
			}
			currentType = currentType.getSuperclass();
		} while (currentType != null);
	}
}
