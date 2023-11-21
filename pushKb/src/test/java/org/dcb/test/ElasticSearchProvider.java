package org.dcb.test;

import java.util.Map;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.test.extensions.testresources.TestResourcesPropertyProvider;

@ReflectiveAccess
public class ElasticSearchProvider implements TestResourcesPropertyProvider {
	@Override
	public Map<String, String> provide(Map<String, Object> testProperties) {
		String uri = (String) testProperties.get("elasticsearch.http-hosts");

		return Map.of(
			"micronaut.http.services.search.url", uri
		);
	}
}
