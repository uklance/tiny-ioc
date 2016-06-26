package com.lazan.simpleioc;

import java.util.LinkedList;
import java.util.List;

import com.lazan.simpleioc.internal.DefaultServiceRegistry;

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
		for (ServiceModule module : modules) {
			this.modules.add(module);
		}
		return this;
	}
	
	public ServiceRegistry build() {
		return new DefaultServiceRegistry(modules);
	}
}
