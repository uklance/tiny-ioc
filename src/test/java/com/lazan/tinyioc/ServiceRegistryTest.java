package com.lazan.tinyioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

public class ServiceRegistryTest {
	public static class Child {}

	public static class Parent {
		private final Child child;
		public Parent(Child child) {
			super();
			this.child = child;
		}
	}

	public static class GrandParent {
		private final Parent parent;
		private final Child child;
		
		@Inject
		public GrandParent(Parent parent, Child child) {
			super();
			this.parent = parent;
			this.child = child;
		}
		public GrandParent(Parent parent) {
			super();
			this.parent = parent;
			this.child = null;
		}
	}
	
	public static class NamedStrings {
		private final String string1;
		private final String string2;
		public NamedStrings(@Named("string1") String string1, @Named("string2") String string2) {
			super();
			this.string1 = string1;
			this.string2 = string2;
		}
	}
	
	@Test
	public void testInject() {
		ServiceModule module = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(GrandParent.class);
				binder.bind(Parent.class);
				binder.bind(Child.class);
			}
		};
		ServiceRegistry registry = new ServiceRegistryBuilder().withModule(module).build();
		GrandParent grandParent = registry.getService(GrandParent.class);
		Parent parent = registry.getService(Parent.class);
		Child child = registry.getService(Child.class);
		
		assertNotNull(child);
		assertNotNull(parent);
		assertNotNull(grandParent);
		assertSame(child, parent.child);
		assertSame(child, grandParent.child);
		assertSame(parent, grandParent.parent);
		
		try {
			registry.getService("foo");
			fail();
		} catch (IocException e) {
			assertEquals("No service found for serviceId 'foo'", e.getMessage());
		}
		try {
			registry.getService(String.class);
			fail();
		} catch (IocException e) {
			assertEquals("Found 0 services for serviceType 'java.lang.String', expecting 1", e.getMessage());
		}
		
		try {
			registry.getService("child", String.class);
			fail();
		} catch (IocException e) {
			assertEquals("Incompatible type for serviceId 'child'", e.getMessage());
			assertTrue(e.getCause() instanceof ClassCastException);
		}
	}
	
	@Test
	public void testInjectNamed() {
		ServiceModule module = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(NamedStrings.class);
				binder.bind(String.class, "hello").withServiceId("string1");
				binder.bind(String.class, "world").withServiceId("string2");
			}
		};
		ServiceRegistry registry = new ServiceRegistryBuilder().withModule(module).build();
		NamedStrings ns = registry.getService(NamedStrings.class);
		assertEquals("hello", ns.string1);
		assertEquals("world", ns.string2);
		
		assertEquals(createSet("string1", "string2", "namedStrings"), registry.getServiceIds());
		assertEquals(createSet(String.class, NamedStrings.class), registry.getServiceTypes());
		Map<String, String> expected = new HashMap<>();
		expected.put("string1",  "hello");
		expected.put("string2",  "world");
		assertEquals(expected, registry.getServices(String.class));
	}
	
	private Set<Object> createSet(Object... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	@Test
	public void testOverride() {
		ServiceModule module1 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(String.class, "foo").withServiceId("string1");
			}
		};
		ServiceModule module2 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.override(String.class, "foo-override").withServiceId("string1");
			}
		};
		ServiceRegistry registry = buildRegistry(module1, module2);
		assertEquals("foo-override", registry.getService("string1"));
		
	}
	
	static class Circular1 {
		public Circular1(Circular2 c2) {
		}
	}
	static class Circular2 {
		public Circular2(Circular3 c3) {
		}
	}
	static class Circular3 {
		public Circular3(Circular1 c1) {
		}
	}
	
	@Test
	public void testExceptions() {
		ServiceModule module1 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(Circular1.class);
				binder.bind(Circular2.class);
				binder.bind(Circular3.class);
			}
		};
		try {
			ServiceRegistry registry = buildRegistry(module1);
			registry.getService(Circular1.class);
			fail();
		} catch (IocException e) {
			assertEquals("Circular dependency reference detected [circular1, circular2, circular3, circular1]", e.getMessage());
		}
		
		ServiceModule module2 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(String.class, "foo").withServiceId("string1");
			}
		};
		ServiceModule module3 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.override(String.class, "foo-override").withServiceId("string1");
			}
		};
		try {
			buildRegistry(module2, module2);
			fail();
		} catch (IocException e) {
			assertEquals("Duplicate serviceId 'string1'", e.getMessage());
		}
		try {
			buildRegistry(module2, module3, module3);
			fail();
		} catch (IocException e) {
			assertEquals("Duplicate override for serviceId 'string1'", e.getMessage());
		}
		try {
			buildRegistry(module3);
			fail();
		} catch (IocException e) {
			assertEquals("Attempted to override unknown serviceId 'string1'", e.getMessage());
		}
	}
	
	static class StringDecorator implements ServiceDecorator<String> {
		private final String pattern;

		public StringDecorator(String pattern) {
			super();
			this.pattern = pattern;
		}
		
		@Override
		public String decorate(String delegate, ServiceBuilderContext<String> context) {
			return String.format(pattern, delegate);
		}
	}
	
	@Test
	public void testDecorate() throws Exception {
		ServiceModule module1 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(String.class, "hello1");
			}
		};
		ServiceModule module2 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.decorate(String.class, new StringDecorator("one-%s-one"));
			}
		};
		ServiceModule module3 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(String.class, "hello2").withServiceId("string2");
				binder.bind(String.class, "hello3").withServiceId("string3");
				binder.decorate(String.class, new StringDecorator("two-%s-two")).withServiceId("string2");
				binder.decorate(String.class, new StringDecorator("three-%s-three")).withServiceId("string3");
			}
		};
		assertEquals("one-hello1-one", buildRegistry(module1, module2).getService(String.class));
		
		ServiceRegistry registry3 = buildRegistry(module3);
		assertEquals("two-hello2-two", registry3.getService("string2"));
		assertEquals("three-hello3-three", registry3.getService("string3"));
		
		try {
			buildRegistry(module1, module2, module2).getService(String.class);
			fail();
		} catch (IocException e) {
			assertEquals("Duplicate override for serviceId 'string'", e.getMessage());
		}
		try {
			buildRegistry(module2).getService(String.class);
			fail();
		} catch (IocException e) {
			assertEquals("Attempted to decorate unknown serviceId 'string'", e.getMessage());
		}
	}

	private ServiceRegistry buildRegistry(ServiceModule... modules) {
		return new ServiceRegistryBuilder().withModules(modules).build();
	}
	
	public static class TestModule implements ServiceModule {
		@Override
		public void bind(ServiceBinder binder) {
			binder.bind(String.class, "foo");
		}
	}
	
	@Test
	public void testWithModuleType() {
		String string = new ServiceRegistryBuilder()
				.withModuleType(TestModule.class)
				.build()
				.getService(String.class);
		
		assertEquals("foo", string);
	}
}
