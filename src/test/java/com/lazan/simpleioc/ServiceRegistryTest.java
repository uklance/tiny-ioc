package com.lazan.simpleioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

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
	
	@Test
	public void testDecorate() throws Exception {
		ServiceModule module1 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.bind(Reader.class, new StringReader("hello"));
			}
		};
		ServiceModule module2 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.decorate(Reader.class, new ServiceDecorator<Reader>() {
					@Override
					public Reader decorate(Reader candidate, ServiceBuilderContext context) {
						return new BufferedReader(candidate);
					}
				});
			}
		};
		ServiceModule module3 = new ServiceModule() {
			@Override
			public void bind(ServiceBinder binder) {
				binder.decorate("reader", new ServiceDecorator<Reader>() {
					@Override
					public Reader decorate(Reader candidate, ServiceBuilderContext context) {
						return new BufferedReader(candidate);
					}
				});
			}
		};
		Reader reader1 = buildRegistry(module1).getService(Reader.class);
		Reader reader2 = buildRegistry(module1, module2).getService(Reader.class);
		Reader reader3 = buildRegistry(module1, module3).getService(Reader.class);
		assertEquals(StringReader.class, reader1.getClass());
		assertEquals(BufferedReader.class, reader2.getClass());
		assertEquals(BufferedReader.class, reader3.getClass());
		assertEquals("hello", ((BufferedReader) reader2).readLine());
		assertEquals("hello", ((BufferedReader) reader3).readLine());
		try {
			buildRegistry(module1, module2, module2).getService(Reader.class);
			fail();
		} catch (IocException e) {
			assertEquals("Multiple decorators found for serviceType 'java.io.Reader'", e.getMessage());
		}
		try {
			buildRegistry(module1, module3, module3).getService(Reader.class);
			fail();
		} catch (IocException e) {
			assertEquals("Multiple decorators found for serviceId 'reader'", e.getMessage());
		}
		try {
			buildRegistry(module1, module2, module3).getService(Reader.class);
			fail();
		} catch (IocException e) {
			assertEquals("Conflicting decorators registered for serviceId 'reader' and serviceType 'java.io.Reader'", e.getMessage());
		}
		try {
			buildRegistry(module2).getService(Reader.class);
			fail();
		} catch (IocException e) {
			assertEquals("Attempted to decorate unknown serviceType 'java.io.Reader'", e.getMessage());
		}
		try {
			buildRegistry(module3).getService(Reader.class);
			fail();
		} catch (IocException e) {
			assertEquals("Attempted to decorate unknown serviceId 'reader'", e.getMessage());
		}
	}

	private ServiceRegistry buildRegistry(ServiceModule... modules) {
		return new ServiceRegistryBuilder().withModules(modules).build();
	}
}
