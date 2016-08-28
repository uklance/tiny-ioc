package com.lazan.tinyioc;

public interface OrderedContributionOptions {
	OrderedContributionOptions before(String contributionId);
	OrderedContributionOptions after(String contributionId);
}
