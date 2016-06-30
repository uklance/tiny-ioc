package com.lazan.tinyioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.inject.Named;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class InjectionServiceBuilder<T> implements ServiceBuilder<T> {
	private final Class<T> concreteType;
	
	public InjectionServiceBuilder(Class<T> concreteType) {
		super();
		this.concreteType = concreteType;
	}

	@Override
	public T build(ServiceBuilderContext<T> context) {
		try {
			Constructor<T> constructor = findConstructor(concreteType);
			if (constructor.getParameterTypes().length == 0) {
				return constructor.newInstance();
			}
			Object[] params = createConstructorParameters(constructor, context);
			T service = constructor.newInstance(params);
			injectFields(service, context);
			return service;
		} catch (IocException e) {
			throw e;
		} catch (Exception e) {
			throw new IocException(e, "Error building service '%s'", context.getServiceId());
		}
	}
	
	protected Object[] createConstructorParameters(Constructor<T> constructor, ServiceBuilderContext<T> context) {
		Class[] paramTypes = constructor.getParameterTypes();
		ServiceRegistry registry = context.getServiceRegistry();
		Object[] params = new Object[paramTypes.length];
		Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
		for (int i = 0; i < paramTypes.length; ++i) {
			Class<?> paramType = paramTypes[i];
			Annotation[] anns = paramAnnotations[i];
			Named named = findAnnotation(anns, Named.class);
			Object param;
			if (named != null) {
				param = registry.getService(named.value(), paramType);
			} else {
				param = registry.getService(paramType);
			}
			params[i] = param;
		}
		return params;
	}

	protected <A extends Annotation> A findAnnotation(Annotation[] anns, Class<A> type) {
		for (Annotation ann : anns) {
			if (type.equals(ann.annotationType())) {
				return type.cast(ann);
			}
		}
		return null;
	}

	protected Constructor<T> findConstructor(Class<T> concreteType) {
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

	protected void injectFields(T service, ServiceBuilderContext<T> context) {
		Class<?> currentType = context.getServiceType();
		ServiceRegistry registry = context.getServiceRegistry();
		while (currentType != null){ 
			for (Field field : currentType.getDeclaredFields()) {
				if (field.getAnnotation(Inject.class) != null) {
					Named named = field.getAnnotation(Named.class);
					try {
						Object value;
						if (named != null) {
							value = registry.getService(named.value(), field.getType());
						} else {
							value = registry.getService(field.getType());
						}
						field.setAccessible(true);
						field.set(service, value);
					} catch (Exception e) {
						throw new IocException(e, "Error injecting field '%s' in serviceId '%s'", field.getName(), context.getServiceId());
					}
				}
			}
			currentType = currentType.getSuperclass();
		}
	}
}
