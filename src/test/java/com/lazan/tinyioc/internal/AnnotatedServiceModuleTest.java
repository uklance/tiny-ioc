package com.lazan.tinyioc.internal;

import static org.junit.Assert.assertEquals;

import java.sql.Date;

import javax.inject.Named;

import org.junit.Test;

import com.lazan.tinyioc.ServiceBinder;
import com.lazan.tinyioc.ServiceRegistry;
import com.lazan.tinyioc.ServiceRegistryBuilder;
import com.lazan.tinyioc.annotations.Bind;
import com.lazan.tinyioc.annotations.Decorate;
import com.lazan.tinyioc.annotations.Service;
import com.lazan.tinyioc.annotations.ServiceOverride;

public class AnnotatedServiceModuleTest {
	public static class TestModule {
		@Bind
		public void bind(ServiceBinder binder) {
			binder.bind(String.class, "foo").withServiceId("string1");
			binder.bind(String.class, "bar").withServiceId("string2");
			binder.bind(String.class, "baz").withServiceId("string3");
			binder.bind(Long.class, 200L);
		}
		
		@Service
		public Date createDate(Integer integer) {
			return new Date(integer);
		}
		
		@ServiceOverride("string2")
		public String overrideString2(@Named("string1") String string1) {
			return string1 + "x";
		}
		
		@Service
		public Integer createInteger() {
			return 1000;
		}
		
		@Decorate
		public Long decorateLong(Long delegate, Integer integer) {
			return delegate + integer;
		}
	}

	@Test
	public void testAnnotatedModule() {
		ServiceRegistry registry = new ServiceRegistryBuilder().withModule(new AnnotatedServiceModule(TestModule.class)).build();
		assertEquals("foo", registry.getService("string1"));
		assertEquals("foox", registry.getService("string2"));
		assertEquals("baz", registry.getService("string3"));
		assertEquals(1000, registry.getService(Date.class).getTime());
		assertEquals(1000, registry.getService(Integer.class).intValue());
		assertEquals(1200, registry.getService(Long.class).longValue());
	}
	
	public static class DecoratorModule {
		@Bind
		public void bind(ServiceBinder binder) {
			binder.bind(String.class, "foo").withServiceId("string1");
			binder.bind(String.class, "bar").withServiceId("string2");
			binder.bind(String.class, "baz").withServiceId("string3");
		}
		
		@Decorate("string1")
		public String decorateString1(@Named("string1") String string1) {
			return string1 + "x";
		}

		@Decorate("string2")
		public String decorateString2(@Named("string2") String string2, @Named("string3") String string3) {
			return string2 + "y" + string3;
		}
	}
	
	
	@Test
	public void testDecorateNonUniqueServiceType() {
		ServiceRegistry registry = new ServiceRegistryBuilder()
				.withModule(new AnnotatedServiceModule(DecoratorModule.class))
				.build();
		assertEquals("foox", registry.getService("string1"));
		assertEquals("barybaz", registry.getService("string2"));
	}
}
