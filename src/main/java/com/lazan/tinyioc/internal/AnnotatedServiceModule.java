package com.lazan.tinyioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;
import com.lazan.tinyioc.ServiceModule;
import com.lazan.tinyioc.ServiceRegistry;
import com.lazan.tinyioc.annotations.Bind;
import com.lazan.tinyioc.annotations.Decorate;
import com.lazan.tinyioc.annotations.Service;
import com.lazan.tinyioc.annotations.ServiceOverride;

public class AnnotatedServiceModule implements ServiceModule {
	private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(Bind.class, Decorate.class, Service.class, ServiceOverride.class));
	
	private final Class<?> moduleType;

	public AnnotatedServiceModule(Class<?> moduleType) {
		super();
		this.moduleType = moduleType;
	}
	
	@Override
	public void bind(ServiceBinder binder) {
		Object[] instanceRef = new Object[1];
		for (Method method : moduleType.getMethods()) {
			List<Annotation> anns = new ArrayList<>();
			for (Annotation ann : method.getAnnotations()) {
				if (SUPPORTED_TYPES.contains(ann.annotationType())) {
					anns.add(ann);
				}
			}
			if (anns.size() > 1) {
				throw new IocException("Found %s supported annotations on %s.%s", anns.size(), moduleType.getName(), method.getName());
			}
			if (anns.size() == 1) {
				Object instance = getInstance(instanceRef, method);
				Annotation ann = anns.get(0);
				Class<? extends Annotation> annType = ann.annotationType();
				if (annType.equals(Bind.class)) {
					bind(instance, method, binder); 
				} else if (annType.equals(Service.class)) {
					service(instance, method, binder);
				} else if (annType.equals(ServiceOverride.class)) {
					serviceOverride(instance, method, binder);
				} else if (annType.equals(Decorate.class)) {
					decorate(instance, method, binder);
				}
			}
		}
	}

	private Object getInstance(Object[] instanceRef, Method method) {
		Object instance = null;
		if (!Modifier.isStatic(method.getModifiers())) {
			if (instanceRef[0] == null) {
				try {
					instanceRef[0] = moduleType.newInstance();
				} catch (Exception e) {
					throw new IocException(e, "Error instantiating annotated class");
				}
			}
			instance = instanceRef[0];
		}
		return instance;
	}

	protected void bind(Object instance, Method method, ServiceBinder binder) {
		if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(ServiceBinder.class)) {
			try {
				method.invoke(instance, new Object[] { binder });
			} catch (Exception e) {
				throw new IocException(e, "Error invoking %s.%s", method.getDeclaringClass().getName(), method.getName());
			}
		} else {
			throw new IocException("Incompatible parameter types for @Bind method %s.%s, expected single ServiceBinder parameter", method.getDeclaringClass().getName(), method.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void service(Object instance, Method method, ServiceBinder binder) {
		ServiceBinderOptions options = binder.bind(method.getReturnType(), createServiceBuilder(instance, method));
		String serviceId = method.getAnnotation(Service.class).value();
		if (!serviceId.isEmpty()) {
			options.withServiceId(serviceId);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void serviceOverride(Object instance, Method method, ServiceBinder binder) {
		ServiceBinderOptions options = binder.override(method.getReturnType(), createServiceBuilder(instance, method));
		String serviceId = method.getAnnotation(ServiceOverride.class).value();
		if (!serviceId.isEmpty()) {
			options.withServiceId(serviceId);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected ServiceBuilder createServiceBuilder(Object instance, final Method method) {
		return new ServiceBuilder() {
			@Override
			public Object build(ServiceBuilderContext context) {
				Object[] params = getParameters(method, context, null);
				try {
					return method.invoke(instance, params);
				} catch (Exception e) {
					throw new IocException(e, "Error building service '%s'", context.getServiceId());
				}
			}
		};
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void decorate(Object instance, Method method, ServiceBinder binder) {
		ServiceDecoratorOptions options = binder.decorate(method.getReturnType(), new ServiceDecorator() {
			public Object decorate(Object delegate, ServiceBuilderContext context) {
				Object[] params = getParameters(method, context, delegate);
				try {
					return method.invoke(instance, params);
				} catch (Exception e) {
					throw new IocException(e, "Error building service '%s'", context.getServiceId());
				}
			}
		});
		Named named = findAnnotation(method.getAnnotations(), Named.class);
		if (named != null) {
			options.withServiceId(named.value());
		}
	}
	
	protected <A extends Annotation> A findAnnotation(Annotation[] anns, Class<A> type) {
		for (Annotation ann : anns) {
			if (type.equals(ann.annotationType())) {
				return type.cast(ann);
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	protected Object[] getParameters(Method method, ServiceBuilderContext context, Object delegate) {
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length == 0) {
			return null;
		}
		ServiceRegistry registry = context.getServiceRegistry();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; ++i) {
			Class<?> paramType = paramTypes[i];
			
			Object param;
			Named named = findAnnotation(paramAnnotations[i], Named.class);
			boolean useDelegate = delegate != null && paramType.equals(context.getServiceType()) && (named == null || named.value().equals(context.getServiceId()));
			if (useDelegate) {
				param = delegate;
			} else  if (named != null) {
				param = registry.getService(named.value(), paramType);
			} else {
				param = registry.getService(paramType);
			}
			params[i] = param;
		}
		return params;
	}
}