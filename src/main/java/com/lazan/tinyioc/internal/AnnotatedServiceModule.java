package com.lazan.tinyioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final Class<?> moduleType;
	
	private static Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = new HashMap<>();
	static {
		HANDLERS.put(Bind.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, ServiceBinder binder) {
				module.bind(instance, method, binder);
			}
		});
		HANDLERS.put(Service.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, ServiceBinder binder) {
				module.service(instance, method, binder);
			}
		});
		HANDLERS.put(ServiceOverride.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, ServiceBinder binder) {
				module.serviceOverride(instance, method, binder);
			}
		});
		HANDLERS.put(Decorate.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, ServiceBinder binder) {
				module.decorate(instance, method, binder);
			}
		});
	}

	private static interface AnnotationHandler {
		void handle(AnnotatedServiceModule module, Object instance, Method method, ServiceBinder binder);
	}
	
	public AnnotatedServiceModule(Class<?> moduleType) {
		super();
		this.moduleType = moduleType;
	}
	
	@Override
	public void bind(ServiceBinder binder) {
		Object[] instanceRef = new Object[1];
		for (Method method : moduleType.getMethods()) {
			Annotation handleMe = null;
			for (Annotation ann : method.getAnnotations()) {
				if (HANDLERS.containsKey(ann.annotationType())) {
					if (handleMe != null) {
						throw new IocException("Found %s and %s on %s.%s", handleMe.annotationType(), ann.annotationType(), moduleType.getName(), method.getName());
					}
					handleMe = ann;
				}
			}
			if (handleMe != null) {
				Object instance = getInstance(instanceRef, method);
				Class<? extends Annotation> annType = handleMe.annotationType();
				HANDLERS.get(annType).handle(this, instance, method, binder);
			}
		}
	}

	protected Object getInstance(Object[] instanceRef, Method method) {
		Object instance = null;
		if (!Modifier.isStatic(method.getModifiers())) {
			if (instanceRef[0] == null) {
				try {
					instanceRef[0] = moduleType.newInstance();
				} catch (Exception e) {
					throw new IocException(e, "Error instantiating %s", moduleType.getSimpleName());
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
				Object[] params = getParameters(method, context);
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
		Decorate annotation = method.getAnnotation(Decorate.class);
		ServiceDecoratorOptions options = binder.decorate(method.getReturnType(), annotation.decoratorId(), new ServiceDecorator() {
			public Object decorate(Object delegate, ServiceBuilderContext context) {
				Object[] params = getParameters(method, context, true, delegate);
				try {
					return method.invoke(instance, params);
				} catch (Exception e) {
					throw new IocException(e, "Error building service '%s'", context.getServiceId());
				}
			}
		});
		String serviceId = method.getAnnotation(Decorate.class).serviceId();
		if (!serviceId.isEmpty()) {
			options.withServiceId(serviceId);
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
	protected Object[] getParameters(Method method, ServiceBuilderContext context) {
		return getParameters(method, context, false, null);
	}
	
	@SuppressWarnings("rawtypes")
	protected Object[] getParameters(Method method, ServiceBuilderContext context, boolean delegateProvided, Object delegate) {
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length == 0) {
			return null;
		}
		ServiceRegistry registry = context.getServiceRegistry();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; ++i) {
			Class<?> paramType = paramTypes[i];
			Named named = findAnnotation(paramAnnotations[i], Named.class);
			boolean useDelegate = false;
			Object param;
			if (delegateProvided) {
				String paramId = named == null ? ServiceRegistryImpl.getDefaultServiceId(paramType) : named.value();
				useDelegate = context.getServiceId().equals(paramId);
			}
			if (useDelegate) {
				param = delegate;
			} else  {
				try {
					if (named != null) {
						param = registry.getService(named.value(), paramType);
					} else {
						param = registry.getService(paramType);
					}
				} catch (IocException e) {
					throw new IocException(e, "Error with argument %s of %s.%s", i, method.getDeclaringClass().getSimpleName(), method.getName());
				}
			}
			params[i] = param;
		}
		return params;
	}
}