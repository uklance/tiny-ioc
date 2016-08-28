package com.lazan.tinyioc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.internal.ContributionType;

@SuppressWarnings("rawtypes")
public interface ServiceBuilderContext<T> {
	String getServiceId();
	Class<T> getServiceType();
	ServiceRegistry getServiceRegistry();
	ContributionType getContributionType();
	Map getMappedContributions();
	Collection getUnorderedContributions();
	List getOrderedContributions();
	Class<?> getContributionKeyType();
	Class<?> getContributionValueType();
}
