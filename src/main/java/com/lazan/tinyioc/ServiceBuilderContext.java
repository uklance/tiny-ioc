package com.lazan.tinyioc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ServiceBuilderContext {
	String getServiceId();
	Class<?> getServiceType();
	ServiceRegistry getServiceRegistry();
	Map getMappedContributions();
	Collection getUnorderedContributions();
	List getOrderedContributions();
}
