package org.dcb.test.fixtures;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpRequest.DELETE;
import static io.micronaut.http.HttpRequest.HEAD;
import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpRequest.PUT;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.List;

import org.dcb.locate.model.client.ElasticsearchSerializer;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

@Singleton
public class ElasticSearchFixture {
	private final HttpClient httpClient;

	private final ElasticsearchSerializer serializer = new ElasticsearchSerializer();

	public ElasticSearchFixture(@Client(id = "search") HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@SneakyThrows
	public void deleteAllDocuments() {
		final var matchAll = new Query.Builder()
			.matchAll(new MatchAllQuery.Builder().build())
			.build();

		final var deleteRequest = new DeleteByQueryRequest.Builder()
			.index("_all")
			.query(matchAll)
			.refresh(true)
			.waitForCompletion(true)
			.build();

		final var uri = UriBuilder.of("/_all/_delete_by_query").build();

		final var request = POST(uri, serializer.serialize(deleteRequest))
			.header(ACCEPT, APPLICATION_JSON);

		final var response = Mono.from(httpClient.retrieve(request, DeleteByQueryResponse.class))
			.block();

		assertThat("Response should not be null", response, is(notNullValue()));

		assertThat("All documents should be deleted",
			response.getDeleted(), is(greaterThanOrEqualTo(0L)));
	}

	@SneakyThrows
	public <T> void indexDocuments(String index, List<T> documents) {
		documents.forEach(document -> indexDocument(index, document));
	}

	@SneakyThrows
	public <T> void indexDocument(String index, T document) {
		final var uri = UriBuilder.of("/" + index + "/_doc")
			.queryParam("refresh", true)
			.build();

		final var request = POST(uri, document)
			.header(ACCEPT, APPLICATION_JSON);

		Mono.from(httpClient.retrieve(request, Object.class))
			.onErrorMap(HttpClientResponseException.class,
				ElasticSearchFixture::mapResponseExceptionToIndexingError)
			.block();
	}

	public void recreateIndex(String index) {
		if (indexExists(index)) {
			deleteIndex(index);
		}

		createIndex(index);
	}

	@SneakyThrows
	private boolean indexExists(String index) {
		final var uri = UriBuilder.of("/" + index).build();

		// Returns 200 when index exists or 404 when it doesn't
		try {
			Mono.from(httpClient.exchange(HEAD(uri))).block();

			return true;
		}
		catch(HttpClientResponseException ex) {
			return ex.getStatus() != NOT_FOUND;
		}
	}

	@SneakyThrows
	private void deleteIndex(String index) {
		final var uri = UriBuilder.of("/" + index).build();

		Mono.from(httpClient.exchange(DELETE(uri))).block();
	}

	@SneakyThrows
	private void createIndex(String index) {
		final var uri = UriBuilder.of("/" + index).build();

		Mono.from(httpClient.exchange(PUT(uri, null))).block();
	}

	@SneakyThrows
	public void defineMappings(String index, PutMappingRequest mappingRequest) {
		final var uri = UriBuilder.of("/" + index + "/_mapping").build();

		final var request = PUT(uri, serializer.serialize(mappingRequest))
			.header(ACCEPT, APPLICATION_JSON);

		Mono.from(httpClient.retrieve(request)).block();
	}

	private static RuntimeException mapResponseExceptionToIndexingError(
		HttpClientResponseException ex) {

		return new RuntimeException("Could not index document: "
			+ ", reason: " + ex.getResponse().getBody(String.class), ex);
	}

	@lombok.Value
	@Serdeable
	public static class DeleteByQueryResponse {
		Long deleted;
	}
	@lombok.Value
	@Serdeable
	public static class BulkResponse {
		Boolean errors;
	}
}
