package org.dcb.test.clients;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.Builder;

@Singleton
public class FacetsClient {
	private final HttpClient httpClient;

	public FacetsClient(@Client("/") HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public FacetResults getFacets(FacetParameters parameters, String accessToken) {
		final var request = GET("search/instances/facets")
			.uri(builder -> builder.queryParam("query", parameters.getQuery()))
			.uri(builder -> builder.queryParam("facet", parameters.getFacet()))
			.header("Authorization", "BEARER " + accessToken);

		final var response = httpClient.toBlocking().exchange(request, FacetResults.class);

		assertThat("Response should not be null", response, is(notNullValue()));

		assertThat("Status should be ok", response.getStatus(), is(OK));

		final var optionalBody = response.getBody();

		assertThat("Should have a body", optionalBody.isPresent(), is(true));

		return optionalBody.get();
	}

	@Serdeable
	@lombok.Value
	public static class FacetResults {
		Map<String, Facet> facets;
	}

	@Serdeable
	@lombok.Value
	public static class Facet {
		List<Value> values;
	}

	@Serdeable
	@lombok.Value
	public static class Value {
		String id;
		Integer totalRecords;
	}

	@Serdeable
	@lombok.Value
	@Builder
	public static class FacetParameters {
		String query;
		String facet;
	}
}
