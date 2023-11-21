package org.dcb.test.clients;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpStatus.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.Builder;

@Singleton
public class SuggestionsClient {
	private final HttpClient httpClient;

	public SuggestionsClient(@Client("/search/instances/suggestions") HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public SuggestionsResponse suggestTitles(SuggestionParameters parameters,
		String accessToken) {

		return findSuggestions("/title", parameters, accessToken);
	}

	public SuggestionsResponse keywordSuggestions(SuggestionParameters parameters,
		String accessToken) {

		return findSuggestions("/keyword", parameters, accessToken);
	}

	public SuggestionsResponse suggestSubjects(SuggestionParameters parameters,
		String accessToken) {

		return findSuggestions("/subject", parameters, accessToken);
	}

	public SuggestionsResponse suggestAuthors(SuggestionParameters parameters,
		String accessToken) {

		return findSuggestions("/author", parameters, accessToken);
	}

	public SuggestionsResponse findSuggestions(String path,
		SuggestionParameters parameters, String accessToken) {

		final var request = GET(path)
			.uri(builder -> builder
				.queryParam("query", parameters.getQueryText())
				.queryParam("limit", parameters.getLimit()))
			.header("Authorization", "BEARER " + accessToken);

		final var response = httpClient.toBlocking().exchange(request,
			SuggestionsResponse.class);

		assertThat("Response should be ok", response.getStatus(), is(OK));

		final var body = response.getBody();

		assertThat("Should have a body", body.isPresent(), is(true));

		return body.get();
	}

	@Serdeable
	@lombok.Value
	public static class SuggestionsResponse {
		@Nullable SuggestionCategories suggestions;
	}

	@Serdeable
	@lombok.Value
	public static class SuggestionCategories {
		@Nullable Suggestions title;
		@Nullable Suggestions subject;
		@Nullable Suggestions author;
	}

	@Serdeable
	@lombok.Value
	public static class Suggestions {
		@Nullable Integer totalRecords;
		@Nullable List<SuggestionTerm> terms;
	}

	@Serdeable
	@lombok.Value
	public static class SuggestionTerm {
		@Nullable String term;
	}

	@Builder
	@lombok.Value
	public static class SuggestionParameters {
		String queryText;
		@Nullable Integer limit;
	}
}
