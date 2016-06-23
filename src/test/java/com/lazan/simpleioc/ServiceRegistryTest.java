package com.lazan.simpleioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;

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
		
		assertSame(child, parent.child);
		assertSame(child, grandParent.child);
		assertSame(parent, grandParent.parent);
	}
	
	@Test
	public void testNamed() {
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
	}

	private ServiceRegistry buildRegistry(ServiceModule... modules) {
		return new ServiceRegistryBuilder().withModules(Arrays.asList(modules)).build();
	}
}