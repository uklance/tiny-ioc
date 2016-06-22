package com.lazan.simpleioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import javax.inject.Inject;
import javax.inject.Named;

import com.lazan.simpleioc.IocException;
import com.lazan.simpleioc.ServiceBuilder;
import com.lazan.simpleioc.ServiceBuilderContext;
import com.lazan.simpleioc.ServiceRegistry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConstructorServiceBuilder<T> implements ServiceBuilder<T> {
	private final Class<T> concreteType;
	
	public ConstructorServiceBuilder(Class<T> concreteType) {
		super();
		this.concreteType = concreteType;
	}

	@Override
	public T build(ServiceBuilderContext context) {
		try {
			Constructor<T> constructor = findConstructor(concreteType);
			ServiceRegistry registry = context.getServiceRegistry();
			Class[] paramTypes = constructor.getParameterTypes();
			if (paramTypes.length == 0) {
				return constructor.newInstance();
			}
			Object[] params = new Object[paramTypes.length];
			Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
			for (int i = 0; i < paramTypes.length; ++i) {
				Class<?> paramType = paramTypes[i];
				Annotation[] anns = paramAnnotations[i];
				Named named = find(anns, Named.class);
				Object param;
				if (named != null) {
					param = registry.getService(named.value(), paramType);
				} else {
					param = registry.getService(paramType);
				}
				params[i] = param;
			}
			return constructor.newInstance(params);
		} catch (Exception e) {
			throw new IocException(e, "Could not build service %s", context.getServiceId());
		}
	}

	protected <A extends Annotation> A find(Annotation[] anns, Class<A> type) {
		for (Annotation ann : anns) {
			if (type.equals(ann.getClass())) {
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
}
