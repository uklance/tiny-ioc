package com.lazan.tinyioc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lazan.tinyioc.internal.ContributionType;

public interface ServiceBuilderContext {
	String getServiceId();
	Class<?> getServiceType();
	ServiceRegistry getServiceRegistry();
	ContributionType getContributionType();
	<K, V> Map<K, V> getMappedContributions(Class<K> keyType, Class<V> valueType);
	<V> Collection<V> getUnorderedContributions(Class<V> valueType);
	<V> List<V> getOrderedContributions(Class<V> valueType);
	Class<?> getContributionKeyType();
	Class<?> getContributionValueType();
}
