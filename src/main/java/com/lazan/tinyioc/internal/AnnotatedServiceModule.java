package com.lazan.tinyioc.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.MappedConfiguration;
import com.lazan.tinyioc.MappedContributor;
import com.lazan.tinyioc.OrderedConfiguration;
import com.lazan.tinyioc.OrderedContributor;
import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceBinderOptions;
import com.lazan.tinyioc.ServiceBuilder;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceDecorator;
import com.lazan.tinyioc.ServiceDecoratorOptions;
import com.lazan.tinyioc.ServiceModule;
import com.lazan.tinyioc.ServiceRegistry;
import com.lazan.tinyioc.UnorderedConfiguration;
import com.lazan.tinyioc.UnorderedContributor;
import com.lazan.tinyioc.annotations.Bind;
import com.lazan.tinyioc.annotations.Contribute;
import com.lazan.tinyioc.annotations.Decorate;
import com.lazan.tinyioc.annotations.Service;
import com.lazan.tinyioc.annotations.ServiceOverride;

public class AnnotatedServiceModule implements ServiceModule {
	private final Class<?> moduleType;
	
	private static Map<Class<? extends Annotation>, AnnotationHandler> HANDLERS = new HashMap<>();
	static {
		HANDLERS.put(Bind.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder) {
				module.bind(instance, method, (Bind) annotation, binder);
			}
		});
		HANDLERS.put(Service.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder) {
				module.service(instance, method, (Service) annotation, binder);
			}
		});
		HANDLERS.put(ServiceOverride.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder) {
				module.serviceOverride(instance, method, (ServiceOverride) annotation, binder);
			}
		});
		HANDLERS.put(Decorate.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder) {
				module.decorate(instance, method, (Decorate) annotation, binder);
			}
		});
		HANDLERS.put(Contribute.class, new AnnotationHandler() {
			@Override
			public void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder) {
				module.contribute(instance, method, (Contribute) annotation, binder);
			}
		});		
	}

	private static interface AnnotationHandler {
		void handle(AnnotatedServiceModule module, Object instance, Method method, Annotation annotation, ServiceBinder binder);
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
				HANDLERS.get(annType).handle(this, instance, method, handleMe, binder);
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

	protected void bind(Object instance, Method method, Bind bind, ServiceBinder binder) {
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
	protected void service(Object instance, Method method, Service annotation, ServiceBinder binder) {
		ServiceBinderOptions options = binder.bind(method.getReturnType(), createServiceBuilder(instance, method));
		String serviceId = annotation.serviceId();
		if (!serviceId.isEmpty()) {
			options.withServiceId(serviceId);
		}
		if (annotation.eagerLoad()) {
			options.eagerLoad();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void serviceOverride(Object instance, Method method, ServiceOverride annotation, ServiceBinder binder) {
		ServiceBinderOptions options = binder.override(method.getReturnType(), createServiceBuilder(instance, method));
		String serviceId = annotation.value();
		if (!serviceId.isEmpty()) {
			options.withServiceId(serviceId);
		}
		if (annotation.eagerLoad()) {
			options.eagerLoad();
		}
	}
	
	private static final Set<Class<?>> CONTRIBUTION_TYPES = new LinkedHashSet<>(Arrays.asList(OrderedConfiguration.class, UnorderedConfiguration.class, MappedConfiguration.class));
	
	static interface ParameterProvider {
		boolean canProvide(Named named, Class<?> paramType);
		<T> T provide(Named named, Class<T> paramType);
	}
	
	static class TypeParameterProvider<T> implements ParameterProvider {
		Class<T> type;
		T value;
		public TypeParameterProvider(Class<T> type, T value) {
			super();
			this.type = type;
			this.value = value;
		}
		@Override
		public boolean canProvide(Named named, Class<?> paramType) {
			return named == null && paramType.equals(type);
		}
		@Override
		public <T2> T2 provide(Named named, Class<T2> paramType) {
			return paramType.cast(value);
		}
	}
		
	protected void contribute(Object instance, Method method, Contribute annotation, ServiceBinder binder) {
		Map<Integer, Class<?>> paramIndexes = new LinkedHashMap<>();
		String serviceId = annotation.serviceId().isEmpty() ? null : annotation.serviceId();
		Class<?> serviceType = annotation.serviceType().equals(void.class) ? null : annotation.serviceType();
		int valueCount = (serviceId == null ? 0 : 1) + (serviceType == null ? 0 : 1);
		if (valueCount != 1) {
			throw new IocException("Error with %s.%s. Expected one of serviceId and serviceType, found %s", 
					method.getDeclaringClass().getSimpleName(), method.getName(), valueCount);
		}
		for (int i = 0; i < method.getParameterTypes().length; ++i) {
			Class<?> paramType = method.getParameterTypes()[i];
			if (CONTRIBUTION_TYPES.contains(paramType)) {
				paramIndexes.put(i, paramType);
			}
		}
		if (paramIndexes.size() != 1) {
			throw new IocException("Error with %s.%s. Expected 1 parameter of the types %s, found %s", 
					method.getDeclaringClass().getSimpleName(), method.getName(), CONTRIBUTION_TYPES, paramIndexes.size());
		}
		Class<?> contributionType = paramIndexes.values().iterator().next();
		if (contributionType.equals(OrderedConfiguration.class)) {
			OrderedContributor<?> contributor = new OrderedContributor<Object>() {
				@Override
				public void contribute(ServiceBuilderContext context, OrderedConfiguration<Object> configuration) {
					invoke(method, instance, context, new TypeParameterProvider<>(OrderedConfiguration.class, configuration));
				}
			};
			if (serviceId != null) {
				binder.contribute(serviceId, contributor);
			} else {
				binder.contribute(serviceType, contributor);
			}
		} else if (contributionType.equals(UnorderedConfiguration.class)) {
			UnorderedContributor<?> contributor = new UnorderedContributor<Object>() {
				@Override
				public void contribute(ServiceBuilderContext context, UnorderedConfiguration<Object> configuration) {
					invoke(method, instance, context, new TypeParameterProvider<>(UnorderedConfiguration.class, configuration));
				}
			};
			if (serviceId != null) {
				binder.contribute(serviceId, contributor);
			} else {
				binder.contribute(serviceType, contributor);
			}
		} else {
			MappedContributor<?, ?> contributor = new MappedContributor<Object, Object>() {
				@Override
				public void contribute(ServiceBuilderContext context, MappedConfiguration<Object, Object> configuration) {
					invoke(method, instance, context, new TypeParameterProvider<>(MappedConfiguration.class, configuration));
				}
			};
			if (serviceId != null) {
				binder.contribute(serviceId, contributor);
			} else {
				binder.contribute(serviceType, contributor);
			}			
		}
	}	
	
	@SuppressWarnings("rawtypes")
	protected ServiceBuilder createServiceBuilder(Object instance, final Method method) {
		return new ServiceBuilder() {
			@Override
			public Object build(ServiceBuilderContext context) {
				ParameterProvider provider = new ParameterProvider() {
					@Override
					public boolean canProvide(Named named, Class<?> paramType) {
						return named == null && (paramType.equals(Map.class) || paramType.equals(List.class) || paramType.equals(Collection.class));
					}
					
					@Override
					public <T> T provide(Named named, Class<T> paramType) {
						if (paramType.equals(Map.class)) {
							return paramType.cast(context.getMappedContributions());
						} else if (paramType.equals(List.class)) {
							return paramType.cast(context.getOrderedContributions());
						} else {
							return paramType.cast(context.getUnorderedContributions());
						}
					}
				};
				return invoke(method, instance, context, provider);
			}
		};
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void decorate(Object instance, Method method, Decorate annotation, ServiceBinder binder) {
		ServiceDecoratorOptions options = binder.decorate(method.getReturnType(), annotation.decoratorId(), new ServiceDecorator() {
			public Object decorate(ServiceBuilderContext context, Object delegate) {
				ParameterProvider paramProvider = new ParameterProvider() {
					@Override
					public boolean canProvide(Named named, Class<?> paramType) {
						if (named != null) {
							return context.getServiceId().equals(named.value());
						}	
						return context.getServiceType().equals(paramType);
					}
					@Override
					public <T> T provide(Named named, Class<T> paramType) {
						return paramType.cast(delegate);
					}
				};
				return invoke(method, instance, context, paramProvider);
			}
		});
		if (!annotation.serviceId().isEmpty()) {
			options.withServiceId(annotation.serviceId());
		}
		options.before(annotation.before());
		options.after(annotation.after());
	}
	
	protected <A extends Annotation> A findAnnotation(Annotation[] anns, Class<A> type) {
		for (Annotation ann : anns) {
			if (type.equals(ann.annotationType())) {
				return type.cast(ann);
			}
		}
		return null;
	}

	protected Object invoke(Method method, Object instance, ServiceBuilderContext context, ParameterProvider provider) {
		Class<?>[] paramTypes = method.getParameterTypes();
		ServiceRegistry registry = context.getServiceRegistry();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Object[] params = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; ++i) {
			Class<?> paramType = paramTypes[i];
			Named named = findAnnotation(paramAnnotations[i], Named.class);
			Object param;
			if (provider != null && provider.canProvide(named, paramType)) {
				param = provider.provide(named, paramType);
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
		try {
			return method.invoke(instance, params);
		} catch (Exception e) {
			throw new IocException(e, "Error building '%s'", context.getServiceId());
		}
	}
}