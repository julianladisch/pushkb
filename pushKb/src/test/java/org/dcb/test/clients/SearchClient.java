package org.dcb.test.clients;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.UUID;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.Builder;
import lombok.Value;

@Singleton
public class SearchClient {
	private final HttpClient httpClient;

	public SearchClient(@Client("/") HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public SearchResults search(SearchParameters parameters, String accessToken) {
		final var request = GET("search/instances")
			.uri(builder -> builder.queryParam("query", parameters.getQuery()))
			.uri(builder -> builder.queryParam("limit", parameters.getLimit()))
			.uri(builder -> builder.queryParam("offset", parameters.getOffset()))
			.header("Authorization", "BEARER " + accessToken);

		final var response = httpClient.toBlocking().exchange(request,
			SearchResults.class);

		assertThat("Response should not be null", response, is(notNullValue()));

		assertThat("Response should be ok", response.getStatus(), is(OK));

		final var body = response.getBody();

		assertThat("Should have a body", body.isPresent(), is(true));

		return body.get();
	}

	public Instance findInstance(UUID id, String accessToken) {
		final var request = GET("opac-inventory/instances/" + id)
			.header("Authorization", "BEARER " + accessToken);

		final var response = httpClient.toBlocking().exchange(request, Instance.class);

		assertThat("Response should not be null", response, is(notNullValue()));

		assertThat("Status should be ok", response.getStatus(), is(OK));

		final var optionalBody = response.getBody();

		assertThat("Should have a body", optionalBody.isPresent(), is(true));

		return optionalBody.get();
	}

	@Serdeable
	@Value
	public static class SearchResults {
		@Nullable Integer totalRecords;
		@Nullable List<Instance> instances;
	}

	@Serdeable
	@Value
	public static class Instance {
		@Nullable String id;
		@Nullable String title;
		@Nullable List<Identifier> identifiers;
		@Nullable List<String> isbns;
		@Nullable List<String> issns;
		@Nullable List<Subject> subjects;
		@Nullable List<Contributor> contributors;
		@Nullable List<Publication> publication;
		@Nullable String publicationDate;
		@Nullable Integer publicationYear;
		@Nullable List<String> sourceTypes;
		@Nullable List<String> physicalDescriptions;
	}

	@Serdeable
	@Value
	public static class Identifier {
		@Nullable String type;
		@Nullable String value;
	}

	@Serdeable
	@Value
	public static class Subject {
		@Nullable String value;
	}

	@Serdeable
	@Value
	public static class Contributor {
		@Nullable String name;
	}

	@Serdeable
	@Builder
	@Value
	public static class Publication {
		@Nullable String publisher;
		@Nullable String dateOfPublication;
	}

	@Builder
	@Value
	public static class SearchParameters {
		String query;
		@Builder.Default @Nullable Integer limit = null;
		@Builder.Default @Nullable Integer offset = null;
	}
}
