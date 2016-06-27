package com.lazan.tinyioc.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lazan.tinyioc.IocException;
import com.lazan.tinyioc.ServiceBuilderContext;
import com.lazan.tinyioc.ServiceRegistry;

@RunWith(MockitoJUnitRunner.class)
public class InjectionServiceBuilderTest {
	@Mock
	private ServiceRegistry registry;
	
	public static class NoPublicConstructor {
		private NoPublicConstructor() {}
	}

	public static class SingleValue {
		private String value;
		public SingleValue(String value) {
			this.value = value;
		}
	}

	public static class NamedValues {
		private String value1;
		private String value2;
		
		public NamedValues(@Named("value1") String value1, @Named("value2") String value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
	}
	
	public static class MultipleConstructors {
		public MultipleConstructors() {}
		public MultipleConstructors(String value) {}
	}

	public static class MultipleInjectConstructors {
		@Inject public MultipleInjectConstructors() {}
		@Inject public MultipleInjectConstructors(String value) {}
	}
	
	@Test
	public void testNoPublicConstructor() {
		try {
			new InjectionServiceBuilder<>(NoPublicConstructor.class).build(createServiceBuilderContext());
			fail();
		} catch (IocException e) {
			assertEquals("No public constructors found for type " + NoPublicConstructor.class.getName(), e.getMessage());
		}
	}
	
	@Test
	public void testMultipleConstructors() {
		try {
			new InjectionServiceBuilder<>(MultipleConstructors.class).build(createServiceBuilderContext());
			fail();
		} catch (IocException e) {
			String expected = String.format("Found 2 public constructors for type %s, please annotate one with javax.inject.Inject", MultipleConstructors.class.getName());
			assertEquals(expected, e.getMessage());
		}
	}
	
	@Test
	public void testMultipleInjectConstructors() {
		try {
			new InjectionServiceBuilder<>(MultipleInjectConstructors.class).build(createServiceBuilderContext());
			fail();
		} catch (IocException e) {
			String expected = "Found 2 public constructors annotated with javax.inject.Inject for type " + MultipleInjectConstructors.class.getName(); 
			assertEquals(expected, e.getMessage());
		}
	}
	
	@Test
	public void testSingleValue() {
		when(registry.getService(String.class)).thenReturn("foo");
		SingleValue sv = new InjectionServiceBuilder<>(SingleValue.class).build(createServiceBuilderContext());
		assertEquals("foo", sv.value);
	}	

	@Test
	public void testNamedValues() {
		when(registry.getService("value1", String.class)).thenReturn("foo");
		when(registry.getService("value2", String.class)).thenReturn("bar");
		NamedValues nv = new InjectionServiceBuilder<>(NamedValues.class).build(createServiceBuilderContext());
		assertEquals("foo", nv.value1);
		assertEquals("bar", nv.value2);
	}	

	private ServiceBuilderContext createServiceBuilderContext() {
		return new ServiceBuilderContextImpl(registry, null, null);
	}
}
