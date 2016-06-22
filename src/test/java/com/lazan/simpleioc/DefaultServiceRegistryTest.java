package com.lazan.simpleioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

public class DefaultServiceRegistryTest {
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
		ServiceModule module1 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(GrandParent.class);
				binder.bind(Parent.class);
				binder.bind(Child.class);
			}
		};
		ServiceRegistry registry = new DefaultServiceRegistry(module1);
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
		ServiceRegistry registry = new DefaultServiceRegistry(module);
		NamedStrings ns = registry.getService(NamedStrings.class);
		assertEquals("hello", ns.string1);
		assertEquals("world", ns.string2);
	}
}
