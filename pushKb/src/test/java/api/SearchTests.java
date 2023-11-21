package api;

import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.UNAUTHORIZED;
import static org.dcb.test.InstanceExamples.beneathTheSurface;
import static org.dcb.test.InstanceExamples.blankDocument;
import static org.dcb.test.InstanceExamples.brainOfTheFirm;
import static org.dcb.test.InstanceExamples.educationOfAnIdealist;
import static org.dcb.test.InstanceExamples.emptyDocument;
import static org.dcb.test.InstanceExamples.guardsGuards;
import static org.dcb.test.InstanceExamples.lairOfTheLion;
import static org.dcb.test.InstanceExamples.motherTongue;
import static org.dcb.test.InstanceExamples.nature;
import static org.dcb.test.InstanceExamples.practicalGeography;
import static org.dcb.test.InstanceExamples.prisonersOfGeography;
import static org.dcb.test.InstanceExamples.science;
import static org.dcb.test.InstanceExamples.scientificAmerican;
import static org.dcb.test.InstanceExamples.surfaceDetail;
import static org.dcb.test.InstanceExamples.treasureEverywhere;
import static org.dcb.test.InstanceExamples.wholeBrainChild;
import static org.dcb.test.matchers.BodyMatchers.hasTextBody;
import static org.dcb.test.matchers.InstanceMatchers.hasContributors;
import static org.dcb.test.matchers.InstanceMatchers.hasId;
import static org.dcb.test.matchers.InstanceMatchers.hasIsbnIdentifiers;
import static org.dcb.test.matchers.InstanceMatchers.hasIsbns;
import static org.dcb.test.matchers.InstanceMatchers.hasIssnIdentifiers;
import static org.dcb.test.matchers.InstanceMatchers.hasIssns;
import static org.dcb.test.matchers.InstanceMatchers.hasNoContributors;
import static org.dcb.test.matchers.InstanceMatchers.hasNoIsbns;
import static org.dcb.test.matchers.InstanceMatchers.hasNoIssn;
import static org.dcb.test.matchers.InstanceMatchers.hasNoPhysicalDescriptions;
import static org.dcb.test.matchers.InstanceMatchers.hasNoPublicationDate;
import static org.dcb.test.matchers.InstanceMatchers.hasNoPublicationYear;
import static org.dcb.test.matchers.InstanceMatchers.hasNoPublisher;
import static org.dcb.test.matchers.InstanceMatchers.hasNoSourceTypes;
import static org.dcb.test.matchers.InstanceMatchers.hasNoTitle;
import static org.dcb.test.matchers.InstanceMatchers.hasPhysicalDescriptions;
import static org.dcb.test.matchers.InstanceMatchers.hasPublicationDate;
import static org.dcb.test.matchers.InstanceMatchers.hasPublicationYear;
import static org.dcb.test.matchers.InstanceMatchers.hasPublisher;
import static org.dcb.test.matchers.InstanceMatchers.hasSourceTypes;
import static org.dcb.test.matchers.InstanceMatchers.hasSubjects;
import static org.dcb.test.matchers.InstanceMatchers.hasTitle;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.dcb.test.ElasticSearchProvider;
import org.dcb.test.InstanceIndexer;
import org.dcb.test.clients.LoginClient;
import org.dcb.test.clients.SearchClient;
import org.dcb.test.clients.SearchClient.SearchParameters;
import org.dcb.test.fixtures.ElasticSearchFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
class SearchTests {
	@Inject
	private ElasticSearchFixture elasticSearchFixture;
	@Inject
	private InstanceIndexer instanceIndexer;

	@Inject
	private SearchClient searchClient;
	@Inject
	private LoginClient loginClient;

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
		final var results = search(allInstancesQuery());

		// Assert
		assertThat("Should find some instances", results.getTotalRecords(), is(5));
		assertThat("Should include some instances", results.getInstances(), hasSize(5));

		assertThat("Instances should have expected titles",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Surface detail"),
				hasTitle("Scientific American"),
				hasTitle("Prisoners of geography"),
				hasTitle("Brain of the Firm"),
				hasTitle("Guards! Guards!")));
	}

	@Test
	void shouldBeAbleToSortByTitleAscending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy title/sort.ascending");

		// Assert
		assertThat("Instances should have expected titles in ascending order",
			results.getInstances(), contains(
				hasTitle("Brain of the Firm"),
				hasTitle("Guards! Guards!"),
				hasTitle("Prisoners of geography"),
				hasTitle("Scientific American"),
				hasTitle("Surface detail"),
				hasNoTitle(),
				hasNoTitle()
			));
	}

	@Test
	void shouldBeAbleToSortByTitleDescending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy title/sort.descending");

		// Assert
		assertThat("Instances should have expected titles in ascending order",
			results.getInstances(), contains(
				hasTitle("Surface detail"),
				hasTitle("Scientific American"),
				hasTitle("Prisoners of geography"),
				hasTitle("Guards! Guards!"),
				hasTitle("Brain of the Firm"),
				hasNoTitle(),
				hasNoTitle()
				));
	}

	@Test
	void shouldBeAbleToSortByAuthorAscending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy contributors/sort.ascending");

		// Assert
		assertThat("Instances should be sorted by author in ascending order",
			results.getInstances(), contains(
				allOf(
					hasTitle("Surface detail"),
					hasContributors("Banks, Iain")
				),
				allOf(
					hasTitle("Guards! Guards!"),
					hasContributors("Pratchett, Terry")
				),
				allOf(
					hasTitle("Brain of the Firm"),
					hasContributors("Stafford Beer")
				),
				allOf(
					hasTitle("Prisoners of geography"),
					hasContributors("Tim Marshall", "John Scarlett")
				),
				allOf(
					hasNoTitle(),
					hasNoContributors()
				),
				allOf(
					hasNoTitle(),
					hasNoContributors()
				),
				allOf(
					hasTitle("Scientific American"),
					hasNoContributors()
				)
			));
	}

	@Test
	void shouldBeAbleToSortByAuthorDescending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy contributors/sort.descending");

		// Assert
		assertThat("Instances should be sorted by author in descending order",
			results.getInstances(), contains(
				allOf(
					hasTitle("Prisoners of geography"),
					hasContributors("Tim Marshall", "John Scarlett")
				),
				allOf(
					hasTitle("Brain of the Firm"),
					hasContributors("Stafford Beer")
				),
				allOf(
					hasTitle("Guards! Guards!"),
					hasContributors("Pratchett, Terry")
				),
				allOf(
					hasTitle("Surface detail"),
					hasContributors("Banks, Iain")
				),
				allOf(
					hasNoTitle(),
					hasNoContributors()
				),
				allOf(
					hasNoTitle(),
					hasNoContributors()
				),
				allOf(
					hasTitle("Scientific American"),
					hasNoContributors()
				)
			));
	}

	@Test
	void shouldBeAbleToSortByPublicationYearAscending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy publicationYear/sort.ascending");

		// Assert
		assertThat("Instances should be sorted by publication year in ascending order",
			results.getInstances(), contains(
				allOf(
					hasTitle("Brain of the Firm"),
					hasPublicationYear(1972)
				),
				allOf(
					hasTitle("Guards! Guards!"),
					hasPublicationYear(2008)
					),
				allOf(
					hasTitle("Surface detail"),
					hasPublicationYear(2010)
				),
				allOf(
					hasTitle("Prisoners of geography"),
					hasPublicationYear(2019)
				),
				allOf(
					hasNoTitle(),
					hasNoPublicationYear()
				),
				allOf(
					hasNoTitle(),
					hasNoPublicationYear()
				),
				allOf(
					hasTitle("Scientific American"),
					hasNoPublicationYear()
				)
			));
	}

	@Test
	void shouldBeAbleToSortByPublicationYearDescending() {
		// Arrange
		instanceIndexer.indexDocuments(
			blankDocument(),
			emptyDocument(),
			brainOfTheFirm(),
			scientificAmerican(),
			guardsGuards(),
			surfaceDetail(),
			prisonersOfGeography()
		);

		// Act
		final var results = search(allInstancesQuery() + " sortBy publicationYear/sort.descending");

		// Assert
		assertThat("Instances should be sorted by publication year in descending order",
			results.getInstances(), contains(
				allOf(
					hasTitle("Prisoners of geography"),
					hasPublicationYear(2019)
				),
				allOf(
					hasTitle("Surface detail"),
					hasPublicationYear(2010)
				),
				allOf(
					hasTitle("Guards! Guards!"),
					hasPublicationYear(2008)
				),
				allOf(
					hasTitle("Brain of the Firm"),
					hasPublicationYear(1972)
				),
				allOf(
					hasNoTitle(),
					hasNoPublicationYear()
				),
				allOf(
					hasNoTitle(),
					hasNoPublicationYear()
				),
				allOf(
					hasTitle("Scientific American"),
					hasNoPublicationYear()
				)
			));
	}

	@Test
	void keywordSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			prisonersOfGeography(),
			// Document should not match
			brainOfTheFirm()
		);

		// Act
		final var results = search("@keyword all \"Prisoners\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography")
			));
	}

	@Test
	void shouldOnlyFindInstancesWithAllSearchTermsAcrossKeywordFields() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			prisonersOfGeography(),
			// Document should not match
			practicalGeography(), // only has geography
			educationOfAnIdealist(), // only has politics
			lairOfTheLion() // Only has prisoners
		);

		// Act
		final var results = search("@keyword all \"prisoners politics geography\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography")
			));
	}

	@Test
	void titleSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			surfaceDetail(),
			// Document should not match
			prisonersOfGeography()
		);

		// Act
		final var results = search("title all \"Surface\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Surface detail")
			));
	}

	@Test
	void shouldOnlyFindInstancesWithAllSearchTermsInTheTitle() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			practicalGeography(),
			// Document should not match
			prisonersOfGeography() // Does not include practical
		);

		// Act
		final var results = search("title all \"Practical Geography\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Practical geography")
			));
	}

	@Test
	void isbnSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			guardsGuards(),
			// Document should not match
			surfaceDetail()
		);

		// Act
		final var results = search("isbn = \"9780061020643\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Guards! Guards!")
			));
	}

	@ParameterizedTest
	@ValueSource(strings = { "9780061020643", "0061020648" })
	void shouldBeAbleToFindInstanceUsingEitherISBN(String isbn) {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			guardsGuards(),
			// Document should not match
			surfaceDetail()
		);

		// Act
		final var results = search("isbn = \"" + isbn + "\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Guards! Guards!")
			));
	}

	@Test
	void issnSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			scientificAmerican(),
			// Document should not match
			science()
		);

		// Act
		final var results = search("issn = \"0036-8733\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Scientific American")
			));
	}

	@ParameterizedTest
	@ValueSource(strings = { "0028-0836", "1476-4687" })
	void shouldBeAbleToFindInstanceUsingEitherISSN(String issn) {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			nature(),
			// Document should not match
			scientificAmerican()
		);

		// Act
		final var results = search("issn = \"" + issn + "\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Nature")
			));
	}

	@Test
	void subjectSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			brainOfTheFirm(),
			// Document should not match
			prisonersOfGeography()
		);

		// Act
		final var results = search("subjects all \"Cybernetics\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Brain of the Firm")
			));
	}

	@Test
	void shouldOnlyFindInstancesWithAllTermsInSubjects() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			prisonersOfGeography(),
			// Document should not match
			practicalGeography(), // only has geography
			educationOfAnIdealist() // only has politics
		);

		// Act
		final var results = search("subjects all \"politics geography\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Prisoners of geography")
			));
	}

	@Test
	void contributorsSearchShouldFindSomeInstances() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			guardsGuards(),
			// Document should not match
			beneathTheSurface()
		);

		// Act
		final var results = search("contributors all \"Pratchett\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Guards! Guards!")
			));
	}

	@Test
	void shouldOnlyFindInstancesWithAllTermsInContributors() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Document should match
			motherTongue(),
			// Document should not match
			wholeBrainChild(), // only matches Bryson
			treasureEverywhere() // only matches Bill
		);

		// Act
		final var results = search("contributors all \"bill bryson\"");

		// Assert
		assertThat("Should find only instance", results.getTotalRecords(), is(1));
		assertThat("Should include only instance", results.getInstances(), hasSize(1));

		assertThat("Instances should have expected title",
			results.getInstances(), containsInAnyOrder(
				hasTitle("Mother tongue")
			));
	}

	@Test
	void searchResultsShouldIncludeExpectedProperties() {
		// Arrange
		instanceIndexer.indexDocuments(
			science(),
			guardsGuards(),
			scientificAmerican(),
			prisonersOfGeography(),
			nature()
		);

		// Act
		final var results = search(allInstancesQuery());

		// Assert
		final var instances = results.getInstances();

		assertThat("Instances should have expected properties",
			instances, containsInAnyOrder(
				allOf(
					hasId(),
					hasTitle("Guards! Guards!"),
					hasPublisher("Harper, New York"),
					hasPublicationDate("2008, ©1989"),
					hasPublicationYear(2008),
					hasContributors("Pratchett, Terry"),
					hasIsbnIdentifiers("9780061020643", "0061020648"),
					hasIsbns("9780061020643", "0061020648"),
					hasNoIssn(),
					hasSourceTypes("Book"),
					hasPhysicalDescriptions("355 pages")
				),
				allOf(
					hasId(),
					hasTitle("Scientific American"),
					hasNoPublisher(),
					hasNoContributors(),
					hasNoPublicationDate(),
					hasNoPublicationYear(),
					hasIssnIdentifiers("0036-8733"),
					hasIssns("0036-8733"),
					hasNoIsbns(),
					hasSourceTypes("Journal"),
					hasNoPhysicalDescriptions()
				),
				allOf(
					hasId(),
					hasTitle("Prisoners of geography"),
					hasPublisher("Elliott and Thompson Limited"),
					hasPublicationDate("2019"),
					hasPublicationYear(2019),
					hasIsbnIdentifiers("9781783962433"),
					hasIsbns("9781783962433"),
					hasNoIssn(),
					hasContributors("Tim Marshall", "John Scarlett"),
					hasSubjects("Geopolitics", "Politics and Government", "Geography"),
					hasSourceTypes("Book"),
					hasPhysicalDescriptions("303 pages")
				),
				allOf(
					hasId(),
					hasTitle("Science"),
					hasNoPublisher(),
					hasNoPublicationDate(),
					hasNoPublicationYear(),
					hasNoContributors(),
					hasIssnIdentifiers("0036-8075"),
					hasIssns("0036-8075"),
					hasNoIsbns(),
					hasNoSourceTypes(),
					hasNoPhysicalDescriptions()
				),
				allOf(
					hasId(),
					hasTitle("Nature"),
					hasNoPublisher(),
					hasNoPublicationDate(),
					hasNoPublicationYear(),
					hasNoContributors(),
					hasIssnIdentifiers("0028-0836", "1476-4687"),
					hasIssns("0028-0836", "1476-4687"),
					hasNoIsbns(),
					hasNoSourceTypes(),
					hasNoPhysicalDescriptions()
				)
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
			.query(allInstancesQuery())
			.limit(3)
			.offset(2)
			.build();

		final var results = search(parameters);

		// Assert
		assertThat("Search results should not be null", results, is(notNullValue()));
		assertThat("Should find some instances", results.getTotalRecords(), is((5)));

		assertThat("Should return page limited instances", results.getInstances(), hasSize((3)));
		assertThat("Every result should have a title", results.getInstances(),
			everyItem(hasTitle()));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"title all \"guards!\"",
		"title all \"guards &&\"",
		// Locate already escapes backslash in CQL queries
		"title all \"guards \\\\\""
	})
	void shouldBeAbleToIncludeSpecialCharactersInQuery(String cqlQuery) {
		// Arrange
		instanceIndexer.indexDocuments(guardsGuards());

		// Act
		final var results = search(cqlQuery);

		// Assert
		// Now that title search matches all terms the record count varies for these queries
		assertThat("Should respond with instance results",
			results.getTotalRecords(), is(notNullValue()));
	}

	@Test
	void searchShouldFailWhenCqlContainsUnrecognisedSearchIndex() {
		// Act
		final var parameters = SearchParameters.builder()
			.query("publisher all \"springer\"")
			.build();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> search(parameters));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception,
			"Unrecognised CQL index \"publisher\" in query: \"publisher all \"springer\"\"");
	}

	@Test
	void searchShouldFailWhenCqlContainsUnrecognisedSortIndex() {
		// Act
		final var cql = allInstancesQuery() + " sortBy publisher/sort.ascending";

		final var parameters = SearchParameters.builder()
			.query(cql)
			.build();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> search(parameters));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception,
			"Unrecognised CQL index \"publisher\" in query: \"" + cql + "\"");
	}

	@Test
	void searchShouldFailWhenCqlIsInvalid() {
		// Act
		final var parameters = SearchParameters.builder()
			.query("")
			.build();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> search(parameters));

		assertThat("Response should be bad request",
			exception.getStatus(), is(BAD_REQUEST));

		hasTextBody(exception, "Failed to parse cql: \"\"");
	}

	@Test
	void shouldNotBeAbleToSearchWhenAnonymous() {
		// Act
		final var parameters = SearchParameters.builder()
			.query(allInstancesQuery())
			.build();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> searchClient.search(parameters, null));

		assertThat("Response should be unauthorised",
			exception.getStatus(), is(UNAUTHORIZED));
	}

	@Test
	void shouldBeAbleToFindExistingInstance() {
		// Arrange
		final var instanceToFind = guardsGuards();

		instanceIndexer.indexDocuments(instanceToFind);

		// Act
		final var accessToken = loginClient.login();

		final var instance = searchClient.findInstance(instanceToFind.getId(), accessToken);

		assertThat("Instance should not be null", instance, is(notNullValue()));

		assertThat("Instance should have expected properties", instance, allOf(
			hasId(),
			hasTitle("Guards! Guards!"),
			hasContributors("Pratchett, Terry"),
			hasIsbnIdentifiers("9780061020643", "0061020648"),
			hasIsbns("9780061020643", "0061020648"),
			hasNoIssn(),
			hasPublisher("Harper, New York"),
			hasSourceTypes("Book"),
			hasPhysicalDescriptions("355 pages")
		));
	}

	@Test
	void shouldNotBeAbleToFindUnknownInstance() {
		// Act
		final var accessToken = loginClient.login();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> searchClient.findInstance(UUID.randomUUID(), accessToken));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Status should be not found", exception.getStatus(), is(NOT_FOUND));
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

	private static String allInstancesQuery() {
		return "cql.allRecords=1";
	}
}
