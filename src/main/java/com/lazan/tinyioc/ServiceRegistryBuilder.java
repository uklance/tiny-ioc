package com.lazan.tinyioc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.lazan.tinyioc.internal.ServiceRegistryImpl;

public class ServiceRegistryBuilder {
	private List<ServiceModule> modules = new LinkedList<>();
	
	public ServiceRegistryBuilder withModule(ServiceModule module) {
		modules.add(module);
		return this;
	}
	
	public ServiceRegistryBuilder withModules(Iterable<ServiceModule> modules) {
		for (ServiceModule module : modules) {
			this.modules.add(module);
		}
		return this;
	}
	
	public ServiceRegistryBuilder withModules(ServiceModule... modules) {
		return withModules(Arrays.asList(modules));
	}
	
	public ServiceRegistry build() {
		return new ServiceRegistryImpl(modules);
	}
}
