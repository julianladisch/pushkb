package api;

import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static org.dcb.test.InstanceExamples.beneathTheSurface;
import static org.dcb.test.InstanceExamples.brainOfTheFirm;
import static org.dcb.test.InstanceExamples.darwinsArmada;
import static org.dcb.test.InstanceExamples.educationOfAnIdealist;
import static org.dcb.test.InstanceExamples.guardsGuards;
import static org.dcb.test.InstanceExamples.prisonersOfGeography;
import static org.dcb.test.InstanceExamples.surfaceDetail;
import static org.dcb.test.InstanceExamples.wholeBrainChild;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

import org.dcb.test.ElasticSearchProvider;
import org.dcb.test.InstanceIndexer;
import org.dcb.test.clients.LoginClient;
import org.dcb.test.clients.SuggestionsClient;
import org.dcb.test.clients.SuggestionsClient.SuggestionParameters;
import org.dcb.test.fixtures.ElasticSearchFixture;
import org.hamcrest.Matcher;
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
class SuggestionsTests {
	@Inject
	private ElasticSearchFixture elasticSearchFixture;
	@Inject
	private InstanceIndexer instanceIndexer;

	@Inject
	private SuggestionsClient suggestionsClient;
	@Inject
	private LoginClient loginClient;

	@SneakyThrows
	@BeforeEach
	void beforeEach() {
		elasticSearchFixture.deleteAllDocuments();
		instanceIndexer.defineIndex();
	}

	@Test
	void shouldBeAbleToFindTitleSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should match
			surfaceDetail(),
			beneathTheSurface(),
			// Should not match
			guardsGuards()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("Surface")
			.build();

		final var response = suggestionsClient.suggestTitles(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		final var titleSuggestions = response.getSuggestions().getTitle();

		assertThat("Should have title suggestions",
			titleSuggestions, is(notNullValue()));

		assertThat("Should find some title suggestions",
			titleSuggestions.getTotalRecords(), is(2));

		assertThat("Should find 3 title suggestions", titleSuggestions.getTerms(), hasSize(2));

		assertThat("Should have expected title suggestions",
			titleSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Surface detail"),
				hasTerm("Beneath the Surface")
			));
	}

	@Test
	void shouldBeAbleToLimitTitleSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			surfaceDetail(),
			beneathTheSurface()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("Surface")
			.limit(1)
			.build();

		final var response = suggestionsClient.suggestTitles(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		assertThat("Should have title suggestions",
			response.getSuggestions().getTitle(), is(notNullValue()));

		assertThat("Should find some title suggestions",
			response.getSuggestions().getTitle().getTotalRecords(), is(1));

		final var titleSuggestions = response.getSuggestions().getTitle().getTerms();

		assertThat("Should find 1 title suggestions", titleSuggestions, hasSize(1));

		assertThat("Should have expected title suggestions",
			titleSuggestions, containsInAnyOrder(termContains("Surface")));
	}

	@Test
	void shouldBeAbleToFindSubjectSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should match
			prisonersOfGeography(),
			educationOfAnIdealist(),
			// Should not match
			brainOfTheFirm()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("government")
			.build();

		final var response = suggestionsClient.suggestSubjects(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		final var subjectSuggestions = response.getSuggestions().getSubject();

		assertThat("Should have subject suggestions",
			subjectSuggestions, is(notNullValue()));

		assertThat("Should find some subject suggestions",
			subjectSuggestions.getTotalRecords(), is(4));

		assertThat("Should find 4 subject suggestions", subjectSuggestions.getTerms(), hasSize(4));

		assertThat("Should have expected subject suggestions",
			subjectSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Geopolitics"),
				hasTerm("Autobiographies"),
				hasTerm("Politics and Government"),
				hasTerm("Geography")
			));
	}

	@Test
	void shouldBeAbleToFindAuthorSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should match
			surfaceDetail(),
			darwinsArmada(),
			// Should not match
			brainOfTheFirm()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("Iain")
			.build();

		final var response = suggestionsClient.suggestAuthors(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		final var authorSuggestions = response.getSuggestions().getAuthor();

		assertThat("Should have author suggestions",
			authorSuggestions, is(notNullValue()));

		assertThat("Should find some author suggestions",
			authorSuggestions.getTotalRecords(), is(2));

		assertThat("Should find 2 author suggestions", authorSuggestions.getTerms(), hasSize(2));

		assertThat("Should have expected author suggestions",
			authorSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Banks, Iain"),
				hasTerm("Iain McCalman")
			));
	}

	@Test
	void shouldBeAbleToFindKeywordSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			// Should match
			surfaceDetail(),
			darwinsArmada(),
			// Should not match
			brainOfTheFirm()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("Iain")
			.build();

		final var response = suggestionsClient.keywordSuggestions(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		final var titleSuggestions = response.getSuggestions().getTitle();

		assertThat("Should have title suggestions",
			titleSuggestions, is(notNullValue()));

		assertThat("Should find some title suggestions",
			titleSuggestions.getTotalRecords(), is(2));

		assertThat("Should find 2 title suggestions", titleSuggestions.getTerms(), hasSize(2));

		assertThat("Instances should have expected title suggestions",
			titleSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Surface detail"),
				hasTerm("Darwin's armada")
			));

		final var authorSuggestions = response.getSuggestions().getAuthor();

		assertThat("Should have author suggestions",
			authorSuggestions, is(notNullValue()));

		assertThat("Should find some author suggestions",
			authorSuggestions.getTotalRecords(), is(2));

		assertThat("Should find 2 author suggestions", authorSuggestions.getTerms(), hasSize(2));

		assertThat("Instances should have expected author suggestions",
			authorSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Banks, Iain"),
				hasTerm("Iain McCalman")
			));

		final var subjectSuggestions = response.getSuggestions().getSubject();

		assertThat("Should have subject suggestions",
			subjectSuggestions, is(notNullValue()));

		assertThat("Should find some subject suggestions",
			subjectSuggestions.getTotalRecords(), is(4));

		assertThat("Should find 4 subject suggestions", subjectSuggestions.getTerms(), hasSize(4));

		assertThat("Instances should have expected subject suggestions",
			subjectSuggestions.getTerms(), containsInAnyOrder(
				hasTerm("Biographies"),
				hasTerm("Fiction"),
				hasTerm("Science fiction"),
				hasTerm("Evolution (Biology)")
			));
	}

	@Test
	void shouldBeAbleToLimitKeywordSuggestions() {
		// Arrange
		instanceIndexer.indexDocuments(
			brainOfTheFirm(),
			wholeBrainChild()
		);

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("Brain")
			.limit(1)
			.build();

		final var response = suggestionsClient.keywordSuggestions(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		assertThat("Should have title suggestions",
			response.getSuggestions().getTitle(), is(notNullValue()));

		assertThat("Should find some title suggestions",
			response.getSuggestions().getTitle().getTotalRecords(), is(1));

		final var titleSuggestions = response.getSuggestions().getTitle().getTerms();

		assertThat("Should find 1 title suggestions", titleSuggestions, hasSize(1));

		assertThat("Instances should have expected title suggestions",
			titleSuggestions, containsInAnyOrder(
				termContains("Brain")));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"guards!",
		"guards &&",
		"guards \\"
	})
	void shouldBeAbleToIncludeSpecialCharactersInQuery(String query) {
		// Arrange
		instanceIndexer.indexDocuments(guardsGuards());

		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText(query)
			.limit(1)
			.build();

		final var response = suggestionsClient.suggestTitles(parameters, accessToken);

		// Assert
		assertThat("Should have suggestions",
			response.getSuggestions(), is(notNullValue()));

		assertThat("Should have title suggestions",
			response.getSuggestions().getTitle(), is(notNullValue()));

		assertThat("Should find some title suggestions",
			response.getSuggestions().getTitle().getTotalRecords(), is(1));

		final var titleSuggestions = response.getSuggestions().getTitle().getTerms();

		assertThat("Should find 1 title suggestions", titleSuggestions, hasSize(1));

		assertThat("Instances should have expected title suggestions",
			titleSuggestions, containsInAnyOrder(
				termContains("Guards")));
	}

	@Test
	void shouldNotBeAbleToFindOtherKindsOfSuggestions() {
		// Act
		final var accessToken = loginClient.login();

		final var parameters = SuggestionParameters.builder()
			.queryText("food")
			.limit(1)
			.build();

		final var exception = assertThrows(HttpClientResponseException.class,
			() -> suggestionsClient.findSuggestions("/unknown",
				parameters, accessToken));

		// This is not ideal as the default when a path isn't matched is 403
		// However it is harder to return a 403 response in a nice way
		assertThat(exception.getStatus(), is(NOT_FOUND));
	}

	private static Matcher<SuggestionsClient.SuggestionTerm> hasTerm(String expectedTerm) {
		return hasProperty("term", is(expectedTerm));
	}

	private static Matcher<SuggestionsClient.SuggestionTerm> termContains(String expectedTerm) {
		return hasProperty("term", containsString(expectedTerm));
	}
}
