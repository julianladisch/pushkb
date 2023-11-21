package org.dcb.locate.model.client;

import static org.dcb.test.InstanceExamples.beneathTheSurface;
import static org.dcb.test.InstanceExamples.brainOfTheFirm;
import static org.dcb.test.InstanceExamples.guardsGuards;
import static org.dcb.test.InstanceExamples.prisonersOfGeography;
import static org.dcb.test.InstanceExamples.scientificAmerican;
import static org.dcb.test.InstanceExamples.surfaceDetail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.dcb.test.ElasticSearchProvider;
import org.dcb.test.InstanceIndexer;
import org.dcb.test.fixtures.ElasticSearchFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MicronautTest
@TestResourcesProperties(
	value = "elasticsearch.http-hosts",
	providers = ElasticSearchProvider.class
)
public class HttpInstanceSearchClientTests {
	@Inject
	private ElasticSearchFixture elasticSearchFixture;
	@Inject
	private InstanceIndexer instanceIndexer;

	@Inject
	private HttpInstanceSearchClient searchClient;

	@SneakyThrows
	@BeforeEach
	void beforeEach() {
		elasticSearchFixture.deleteAllDocuments();
		instanceIndexer.defineIndex();
	}

	@Test
	void shouldBeAbleToSearch() {
		// Arrange
		instanceIndexer.indexDocuments(
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = searchClient.search(SearchParameters.builder()
				.query(allRecordsQuery())
				.accurateCount(true)
				.build())
			.block();

		// Assert
		assertThat("Results should not be null", results, is(notNullValue()));
		assertThat("Results should have correct total count", results.getTotalHits(), is(5L));

		final var hits = results.getHits();

		assertThat("Should include some instances", hits, hasSize(5));

		assertThat("Instances should have expected titles", hits,
			containsInAnyOrder(
				hasProperty("title", is("Brain of the Firm")),
				hasProperty("title", is("Scientific American")),
				hasProperty("title", is("Prisoners of geography")),
				hasProperty("title", is("Surface detail")),
				hasProperty("title", is("Guards! Guards!"))
			));
	}

	@Test
	void shouldBeAbleToPageSearchResults() {
		// Arrange
		instanceIndexer.indexDocuments(
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var parameters = SearchParameters.builder()
			.query(allRecordsQuery())
			.limit(3)
			.offset(2)
			.accurateCount(true)
			.build();

		final var results = searchClient.search(parameters).block();

		// Assert
		assertThat("Results should not be null", results, is(notNullValue()));
		assertThat("Results should have correct total count", results.getTotalHits(), is(5L));

		assertThat("Should return page limited instances", results.getHits(), hasSize(3));
	}

	@Test
	void shouldBeAbleToAggregateDocuments() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should match
			surfaceDetail(),
			beneathTheSurface(),
			// Should not match
			guardsGuards()
		);

		// Act
		final var parameters = SearchParameters.builder()
			.query(allRecordsQuery())
			.limit(0)
			.accurateCount(false)
			.aggregations(Map.of("TITLES", Aggregation.of(aggregation ->
						aggregation.terms(TermsAggregation.of(
							term -> term
								.field("title.keyword")
								.size(10))))))
			.build();

		final var results = searchClient.search(parameters).block();

		// Assert
		assertThat("Results should not be null", results, is(notNullValue()));

		assertThat("Aggregations should not be null", results.getAggregations(), is(notNullValue()));
		assertThat("Aggregations contains titles", results.getAggregations().containsKey("TITLES"), is(true));

		final var titles = results.getAggregations().get("TITLES");

		assertThat("Titles aggregate should not be null", titles, is(notNullValue()));
	}

	@Test
	void shouldFailWhenSearchServerRespondsWithBadRequest() {
		// Act
		final var sortByTextProperty = SortOptions.of(
			sort -> sort.field(field -> field.field("primaryAuthor")));

		final var exception = assertThrows(SearchServerBadRequestException.class,
			() -> searchClient.search(SearchParameters.builder()
					.query(allRecordsQuery())
					.accurateCount(true)
					.sortOptions(List.of(sortByTextProperty))
					.build())
				.block());

		// Assert
		assertThat("Should not be null", exception, is(notNullValue()));
		assertThat("Should have a cause", exception.getCause(), is(notNullValue()));

		assertThat("Should have message from server", exception.getMessage(),
			containsString("illegal_argument_exception"));
	}

	private static Query allRecordsQuery() {
		return Query.of(query -> query
			.queryString(queryString -> queryString
				.query("*")));
	}
}
