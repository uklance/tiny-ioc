package com.lazan.tinyioc;

public interface OrderedConfiguration<T> {
	OrderedContributionOptions add(String contributionId, ServiceBuilder<? extends T> builder);
	OrderedContributionOptions add(String contributionId, Class<? extends T> type);
	OrderedContributionOptions add(String contributionId, T value);
}
