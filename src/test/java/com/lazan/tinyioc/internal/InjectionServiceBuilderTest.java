package com.lazan.tinyioc.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Date;

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
	
	public abstract static class AbstractInjectFields {
		@Inject protected String string1;
	}
	
	public static class InjectFields extends AbstractInjectFields {
		private final Integer integer;
		
		public InjectFields(Integer integer) {
			super();
			this.integer = integer;
		}
		@Inject @Named("date1") private Date date1;
		@Inject @Named("date2") private Date date2;
		private String string2;
	}
	
	@Test
	public void testNoPublicConstructor() {
		try {
			build(NoPublicConstructor.class);
			fail();
		} catch (IocException e) {
			assertEquals("No public constructors found for type " + NoPublicConstructor.class.getName(), e.getMessage());
		}
	}
	
	@Test
	public void testMultipleConstructors() {
		try {
			build(MultipleConstructors.class);
			fail();
		} catch (IocException e) {
			String expected = String.format("Found 2 public constructors for type %s, please annotate one with javax.inject.Inject", MultipleConstructors.class.getName());
			assertEquals(expected, e.getMessage());
		}
	}
	
	@Test
	public void testMultipleInjectConstructors() {
		try {
			build(MultipleInjectConstructors.class);
			fail();
		} catch (IocException e) {
			String expected = "Found 2 public constructors annotated with javax.inject.Inject for type " + MultipleInjectConstructors.class.getName(); 
			assertEquals(expected, e.getMessage());
		}
	}
	
	@Test
	public void testSingleValue() {
		when(registry.getService(String.class)).thenReturn("foo");
		SingleValue sv = build(SingleValue.class);
		assertEquals("foo", sv.value);
	}	

	@Test
	public void testNamedValues() {
		when(registry.getService("value1", String.class)).thenReturn("foo");
		when(registry.getService("value2", String.class)).thenReturn("bar");
		NamedValues nv = build(NamedValues.class);
		assertEquals("foo", nv.value1);
		assertEquals("bar", nv.value2);
	}
	
	@Test
	public void testInjectFields() {
		when(registry.getService(Integer.class)).thenReturn(1);
		when(registry.getService("date1", Date.class)).thenReturn(new Date(2));
		when(registry.getService("date2", Date.class)).thenReturn(new Date(3));
		when(registry.getService(String.class)).thenReturn("hello");
		InjectFields values = build(InjectFields.class);
		
		assertEquals(new Integer(1), values.integer);
		assertEquals(2, values.date1.getTime());
		assertEquals(3, values.date2.getTime());
		assertEquals("hello", values.string1);
		assertNull(values.string2);
	}
	
	private <T> T build(Class<T> type) {
		ServiceBuilderContext<T> context = new ServiceBuilderContextImpl<>(registry, null, type);
		return new InjectionServiceBuilder<>(type).build(context);
	}
}
