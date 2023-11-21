package api;

import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static org.dcb.test.InstanceExamples.autumn;
import static org.dcb.test.InstanceExamples.cityOfMirrors;
import static org.dcb.test.InstanceExamples.darwinsArmada;
import static org.dcb.test.InstanceExamples.educationOfAnIdealist;
import static org.dcb.test.InstanceExamples.guardsGuards;
import static org.dcb.test.InstanceExamples.hamnet;
import static org.dcb.test.InstanceExamples.prisonersOfGeography;
import static org.dcb.test.InstanceExamples.scientificAmerican;
import static org.dcb.test.InstanceExamples.surfaceDetail;
import static org.dcb.test.InstanceExamples.trainingDay;
import static org.dcb.test.matchers.BodyMatchers.hasTextBody;
import static org.dcb.test.matchers.InstanceMatchers.hasTitle;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dcb.test.ElasticSearchProvider;
import org.dcb.test.InstanceIndexer;
import org.dcb.test.clients.FacetsClient;
import org.dcb.test.clients.FacetsClient.FacetParameters;
import org.dcb.test.clients.LoginClient;
import org.dcb.test.clients.SearchClient;
import org.dcb.test.clients.SearchClient.SearchParameters;
import org.dcb.test.fixtures.ElasticSearchFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

@MicronautTest
@TestResourcesProperties(
	value = "elasticsearch.http-hosts",
	providers = ElasticSearchProvider.class
)
class FacetingTests {
	@Inject
	private ElasticSearchFixture elasticSearchFixture;
	@Inject
	private InstanceIndexer instanceIndexer;

	@Inject
	private SearchClient searchClient;
	@Inject
	private FacetsClient facetsClient;
	@Inject
	private LoginClient loginClient;

	@SneakyThrows
	@BeforeEach
	void beforeEach() {
		elasticSearchFixture.deleteAllDocuments();
		instanceIndexer.defineIndex();
	}

	@Test
	void shouldBeAbleToGetSubjectFacets() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			educationOfAnIdealist(),
			surfaceDetail()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("title all *")
			.facet("instanceSubjects:100")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var subjectFacets = results.getFacets().get("instanceSubjects");

		assertThat("Result should include subject facets",
			subjectFacets, is(notNullValue()));

		assertThat("Should include multiple subject facets",
			subjectFacets.getValues(), containsInAnyOrder(
				allOf(
					hasProperty("id", is("Fiction")),
					hasProperty("totalRecords", is(1))
				),
				allOf(
					hasProperty("id", is("Science fiction")),
					hasProperty("totalRecords", is(1))
				),
				allOf(
					hasProperty("id", is("Geopolitics")),
					hasProperty("totalRecords", is(1))),
				allOf(
					hasProperty("id", is("Politics and Government")),
					hasProperty("totalRecords", is(2))
				),
				allOf(
					hasProperty("id", is("Autobiographies")),
					hasProperty("totalRecords", is(1))
				),
				allOf(
					hasProperty("id", is("Geography")),
					hasProperty("totalRecords", is(1))
				)
			));
	}

	@Test
	void searchQueryShouldBeRespectedWhenFilterClausesAreAlsoProvided() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			surfaceDetail()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("title all prisoners")
			.facet("instanceSubjects:100")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var subjectFacets = results.getFacets().get("instanceSubjects");

		assertThat("Result should include subject facets",
			subjectFacets, is(notNullValue()));

		assertThat("Should include multiple subject facets",
			subjectFacets.getValues(), containsInAnyOrder(
				hasProperty("id", is("Geopolitics")),
				hasProperty("id", is("Politics and Government")),
				hasProperty("id", is("Geography"))
			));
	}

	@Test
	void shouldBeAbleToLimitSubjectFacets() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			educationOfAnIdealist(),
			surfaceDetail()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("title all *")
			.facet("instanceSubjects:3")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var subjectFacets = results.getFacets().get("instanceSubjects");

		assertThat("Result should include subject facets",
			subjectFacets, is(notNullValue()));

		assertThat("Result should include subject facets",
			subjectFacets.getValues(), hasSize(3));
	}

	@Test
	void shouldBeAbleToFilterSearchBySubject() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should be included
			prisonersOfGeography(),
			educationOfAnIdealist(),
			surfaceDetail(),
			// Should not be included
			scientificAmerican(),
			darwinsArmada()
		);

		// Act
		final var results = search(
			"title all * and instanceSubjects==(\"Politics and Government\" or \"Fiction\")");

		// Assert
		assertThat("Should find some instances", results.getTotalRecords(), is(3));
		assertThat("Should include some instances", results.getInstances(), hasSize(3));

		assertThat("Instances should have expected titles",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography"),
				hasTitle("Education of an idealist"),
				hasTitle("Surface detail")
			));
	}

	@Test
	void shouldBeAbleToGetPublisherFacets() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			guardsGuards()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("title all *")
			.facet("instancePublishers:100")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var publisherFacets = results.getFacets().get("instancePublishers");

		assertThat("Result should include publisher facets",
			publisherFacets, is(notNullValue()));

		assertThat("Should include multiple publisher facets",
			publisherFacets.getValues(), containsInAnyOrder(
				allOf(
					hasProperty("id", is("Elliott and Thompson Limited")),
					hasProperty("totalRecords", is(1))
				),
				allOf(
					hasProperty("id", is("Harper, New York")),
					hasProperty("totalRecords", is(1))
				)
			));
	}

	@Test
	void shouldBeAbleToFilterSearchByPublisher() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should be included
			prisonersOfGeography(),
			// Should not be included
			darwinsArmada(),
			guardsGuards()
		);

		// Act
		final var results = search(
			"title all * and instancePublishers==(\"Elliott and Thompson Limited\")");

		// Assert
		assertThat("Should find some instances", results.getTotalRecords(), is(1));
		assertThat("Should include some instances", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected titles",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography")
			));
	}

	@Test
	void shouldBeAbleToGetFormatFacets() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			guardsGuards(),
			trainingDay(),
			scientificAmerican()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("cql.allRecords = 1")
			.facet("sourceTypes:100")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var publisherFacets = results.getFacets().get("sourceTypes");

		assertThat("Result should include format facets",
			publisherFacets, is(notNullValue()));

		assertThat("Should include multiple format facets",
			publisherFacets.getValues(), containsInAnyOrder(
				allOf(
					hasProperty("id", is("Book")),
					hasProperty("totalRecords", is(2))
				),
				allOf(
					hasProperty("id", is("DVD")),
					hasProperty("totalRecords", is(1))
				),
				allOf(
					hasProperty("id", is("Journal")),
					hasProperty("totalRecords", is(1))
				)
			));
	}

	@Test
	void shouldBeAbleToFilterSearchByFormat() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should be included
			prisonersOfGeography(),
			guardsGuards(),
			trainingDay(),
			// Should not be included
			scientificAmerican(), // Does not match source type
			darwinsArmada() // Has no source type
		);

		// Act
		final var results = search(
			"cql.allRecords = 1 and sourceTypes==(\"Book\" or \"DVD\")");

		// Assert
		assertThat("Instances should have expected titles",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography"),
				hasTitle("Guards! Guards!"),
				hasTitle("Training Day")
			));
	}

	@Test
	void shouldBeAbleToGetMultipleFacetsInTheSameRequest() {
		// Arrange
		instanceIndexer.indexDocuments(
			prisonersOfGeography(),
			guardsGuards(),
			educationOfAnIdealist(),
			surfaceDetail()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = FacetParameters.builder()
			.query("title all *")
			.facet("instancePublishers:100,instanceSubjects:100,sourceTypes:100")
			.build();

		final var results = facetsClient.getFacets(parameters, accessToken);

		// Assert
		assertThat("Result should include facets", results.getFacets(), is(notNullValue()));

		final var publisherFacets = results.getFacets().get("instancePublishers");

		assertThat("Result should include publisher facets",
			publisherFacets, is(notNullValue()));

		assertThat("Result should include some publisher facets",
			publisherFacets.getValues(), hasSize(2));

		final var subjectFacets = results.getFacets().get("instanceSubjects");

		assertThat("Result should include subject facets",
			subjectFacets, is(notNullValue()));

		assertThat("Result should include some subject facets",
			subjectFacets.getValues(), hasSize(6));

		final var formatFacets = results.getFacets().get("sourceTypes");

		assertThat("Result should include format facets",
			formatFacets, is(notNullValue()));

		assertThat("Result should include some format facets",
			formatFacets.getValues(), hasSize(1));
	}

	@Test
	void shouldBeAbleToUseMultipleFiltersInSearch() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should be included
			guardsGuards(),
			prisonersOfGeography(),
			cityOfMirrors(),
			// Should not be included
			darwinsArmada(), // Matches none of format, publisher or subject
			autumn(), // Matches format and publisher, not subject
			hamnet()  // Matches format and subject, not publisher
		);

		// Act
		final var results = search(
			"""
				title all *
				and instancePublishers==("Elliott and Thompson Limited" or "Harper, New York")
				and instanceSubjects==("Politics and Government" or "Fiction")
				and sourceTypes==("Book")"
			""");

		// Assert
		assertThat("Instances should have expected titles",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography"),
				hasTitle("Guards! Guards!"),
				hasTitle("City of Mirrors")
			));
	}

	@Test
	void facetRequestShouldFailWhenFacetListContainsUnexpectedFacet() {
		// Act
		final var parameters = FacetParameters.builder()
			.query("title all prisoners")
			.facet("languages:100")
			.build();

		final var accessToken = loginClient.login();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> facetsClient.getFacets(parameters, accessToken));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception, "Unrecognised facet: \"languages:100\"");
	}

	@Test
	void facetRequestShouldFailWhenFacetListDoesNotIncludeLimit() {
		// Act
		final var parameters = FacetParameters.builder()
			.query("title all prisoners")
			.facet("instanceSubjects")
			.build();

		final var accessToken = loginClient.login();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> facetsClient.getFacets(parameters, accessToken));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception, "Facet does not contain limit: \"instanceSubjects\"");
	}

	@Test
	void facetRequestShouldFailWhenFacetListDoesIncludesLimitThatIsNotNumeric() {
		// Act
		final var parameters = FacetParameters.builder()
			.query("title all prisoners")
			.facet("instanceSubjects:foo")
			.build();

		final var accessToken = loginClient.login();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> facetsClient.getFacets(parameters, accessToken));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception, "Facet limit should be an integer: \"instanceSubjects:foo\"");
	}

	private SearchClient.SearchResults search(String query) {
		return search(SearchParameters.builder()
				.query(query)
				.build()
		);
	}

	private SearchClient.SearchResults search(SearchParameters parameters) {
		final var accessToken = loginClient.login();

		return searchClient.search(parameters, accessToken);
	}
}
