package org.dcb.locate.model.cql;

import static co.elastic.clients.elasticsearch._types.query_dsl.Operator.And;
import static co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.CrossFields;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

class CqlInterpreterTests {
	private final CqlInterpreter cqlInterpreter = new CqlInterpreter();

	@Test
	void shouldInterpretAllRecordsQueryToQueryStringQuery() {
		final var query = interpretCql("cql.allRecords=1");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		assertThat("Query should not have a default operator",
			query.queryString().defaultOperator(), is(nullValue()));
	}

	@Test
	void shouldInterpretAscendingTitleSortSpecification() {
		final var interpretedCql = cqlInterpreter.interpretCql(
			"cql.allRecords=1 sortBy title/sort.ascending");

		final var query = interpretedCql.getQuery();

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		final var sortOptions = interpretedCql.getSortOptions();

		assertThat("Should have single sort option",
			sortOptions, hasSize(1));

		final var onlySortOption = sortOptions.get(0);

		assertThat("Should be field sort",
			onlySortOption.field(), is(notNullValue()));

		assertThat("Sort field should be title",
			onlySortOption.field().field(), is("title.keyword"));

		assertThat("Sort order should be ascending",
			onlySortOption.field().order(), is(SortOrder.Asc));
	}

	@Test
	void shouldInterpretDescendingTitleSortSpecification() {
		final var interpretedCql = cqlInterpreter.interpretCql(
			"cql.allRecords=1 sortBy title/sort.descending");

		final var query = interpretedCql.getQuery();

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		final var sortOptions = interpretedCql.getSortOptions();

		assertThat("Should have single sort option",
			sortOptions, hasSize(1));

		final var onlySortOption = sortOptions.get(0);

		assertThat("Should be field sort",
			onlySortOption.field(), is(notNullValue()));

		assertThat("Sort field should be title",
			onlySortOption.field().field(), is("title.keyword"));

		assertThat("Sort order should be descending",
			onlySortOption.field().order(), is(SortOrder.Desc));
	}

	@Test
	void shouldInterpretAscendingAuthorSortSpecification() {
		final var interpretedCql = cqlInterpreter.interpretCql(
			"cql.allRecords=1 sortBy contributors/sort.ascending");

		final var query = interpretedCql.getQuery();

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		final var sortOptions = interpretedCql.getSortOptions();

		assertThat("Should have single sort option",
			sortOptions, hasSize(1));

		final var onlySortOption = sortOptions.get(0);

		assertThat("Should be field sort",
			onlySortOption.field(), is(notNullValue()));

		assertThat("Sort field should be primary author",
			onlySortOption.field().field(), is("primaryAuthor.keyword"));

		assertThat("Sort order should be ascending",
			onlySortOption.field().order(), is(SortOrder.Asc));
	}

	@Test
	void shouldInterpretAscendingPublicationYearSortSpecification() {
		final var interpretedCql = cqlInterpreter.interpretCql(
			"cql.allRecords=1 sortBy publicationYear/sort.ascending");

		final var query = interpretedCql.getQuery();

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		final var sortOptions = interpretedCql.getSortOptions();

		assertThat("Should have single sort option",
			sortOptions, hasSize(1));

		final var onlySortOption = sortOptions.get(0);

		assertThat("Should be field sort",
			onlySortOption.field(), is(notNullValue()));

		assertThat("Sort field should be publication year",
			onlySortOption.field().field(), is("yearOfPublication"));

		assertThat("Sort order should be ascending",
			onlySortOption.field().order(), is(SortOrder.Asc));
	}
	@Test
	void shouldDefaultSortOrderToAscending() {
		final var interpretedCql = cqlInterpreter.interpretCql(
			"cql.allRecords=1 sortBy title");

		final var query = interpretedCql.getQuery();

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Query should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be wildcard",
			query.queryString().query(), is("*"));

		final var sortOptions = interpretedCql.getSortOptions();

		assertThat("Should have single sort option",
			sortOptions, hasSize(1));

		final var onlySortOption = sortOptions.get(0);

		assertThat("Should be field sort",
			onlySortOption.field(), is(notNullValue()));

		assertThat("Sort field should be title",
			onlySortOption.field().field(), is("title.keyword"));

		assertThat("Sort order should be ascending",
			onlySortOption.field().order(), is(SortOrder.Asc));
	}

	@Test
	void shouldInterpretKeywordQueryToQueryStringQuery() {
		final var query = interpretCql("@keyword all \"brain\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be CQL term only",
			query.queryString().query(), is("brain"));

		assertThat("Query should use the AND operator",
			query.queryString().defaultOperator(), is(And));

		assertThat("Query should use the cross fields type",
			query.queryString().type(), is(CrossFields));
	}

	@Test
	void shouldInterpretTitleQueryToQueryStringQuery() {
		final var query = interpretCql("title all \"surface\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be field and CQL term",
			query.queryString().query(), is("title:surface"));

		assertThat("Query should use the AND operator",
			query.queryString().defaultOperator(), is(And));
	}

	@Test
	void shouldInterpretContributorsQueryToQueryStringQuery() {
		final var query = interpretCql("contributors all \"terry pratchett\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be field and CQL term",
			query.queryString().query(), is("metadata.agents.label:terry pratchett"));

		assertThat("Query should use the AND operator",
			query.queryString().defaultOperator(), is(And));
	}

	@Test
	void shouldInterpretSubjectsQueryToQueryStringQuery() {
		final var query = interpretCql("subjects all \"food health\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be field and CQL terms",
			query.queryString().query(), is("metadata.subjects.label:food health"));

		assertThat("Query should use the AND operator",
			query.queryString().defaultOperator(), is(And));
	}

	@Test
	void shouldInterpretISBNQueryToTermQuery() {
		final var query = interpretCql("isbn = \"9780061020643\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		final var termQuery = query.term();

		assertThat("Should be a term query",
			termQuery, is(notNullValue()));

		assertThat("Field should be ISBN keyword",
			termQuery.field(), is("isbn.keyword"));

		final var termQueryValue = termQuery.value();

		assertThat("Term query value should not be null",
			termQueryValue, is(notNullValue()));

		assertThat("Term query value should be CQL term only",
			termQueryValue.stringValue(), is("9780061020643"));
	}

	@Test
	void shouldInterpretISSNQueryToTermQuery() {
		final var query = interpretCql("issn = \"0036-8733\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		final var termQuery = query.term();

		assertThat("Should be a term query",
			termQuery, is(notNullValue()));

		assertThat("Field should be ISSN keyword",
			termQuery.field(), is("issn.keyword"));

		final var termQueryValue = termQuery.value();

		assertThat("Term query value should not be null",
			termQueryValue, is(notNullValue()));

		assertThat("Term query value should be CQL term only",
			termQueryValue.stringValue(), is("0036-8733"));
	}

	@Test
	void shouldFailWhenQueryIncludesUnrecognisedSearchIndex() {
		final var exception = assertThrows(UnrecognisedCqlIndexException.class,
			() -> interpretCql("publisher all springer"));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unrecognised CQL index \"publisher\" in query: \"publisher all springer\""));

		assertThat("Exception cause should be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldInterpretMultipleSearchClauses() {
		final var query = interpretCql(
			"title all \"surface\" and contributors all \"pratchett\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		final var boolQuery = query.bool();

		assertThat("Should be a boolean query", boolQuery, is(notNullValue()));

		assertThat("Should contain two must queries", boolQuery.must(), hasSize(2));

		final var titleQuery = boolQuery.must().get(0);

		assertThat("Search query should not be null",
			titleQuery.queryString(), is(notNullValue()));

		assertThat("Search query text should be title query",
			titleQuery.queryString().query(), is("title:surface"));

		final var contributorsQuery = boolQuery.must().get(1);

		assertThat("Only query text should be CQL term only",
			contributorsQuery.queryString().query(), is("metadata.agents.label:pratchett"));
	}

	@Test
	void shouldInterpretSingleValueSubjectsFilterQuery() {
		final var query = interpretCql("instanceSubjects==(\"politics\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for subjects",
			filterQuery.field(), is("metadata.subjects.label.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include only term",
			termValues, containsInAnyOrder("politics"));
	}

	@Test
	void shouldInterpretMultipleValueSubjectsFilterQuery() {
		final var query = interpretCql(
			"instanceSubjects==(\"Politics\" or \"Government\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for subjects",
			filterQuery.field(), is("metadata.subjects.label.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include multiple terms",
			termValues, containsInAnyOrder("Politics", "Government"));
	}

	@Test
	void shouldInterpretSingleValuePublishersFilterQuery() {
		final var query = interpretCql("instancePublishers==(\"Penguin\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for publishers",
			filterQuery.field(), is("publisher.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include only term",
			termValues, containsInAnyOrder("Penguin"));
	}

	@Test
	void shouldInterpretMultipleValuePublishersFilterQuery() {
		final var query = interpretCql(
			"instancePublishers==(\"Orbit\" or \"Penguin\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for publishers",
			filterQuery.field(), is("publisher.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include multiple terms",
			termValues, containsInAnyOrder("Orbit", "Penguin"));
	}

	@Test
	void shouldInterpretSingleValueFormatsFilterQuery() {
		final var query = interpretCql("sourceTypes==(\"DVD\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for subjects",
			filterQuery.field(), is("metadata.derivedType.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include only term",
			termValues, containsInAnyOrder("DVD"));
	}

	@Test
	void shouldInterpretMultipleValueFormatsFilterQuery() {
		final var query = interpretCql(
			"sourceTypes==(\"DVD\" or \"Journal\" or \"Book\")");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a boolean query", query.bool(), is(notNullValue()));
		assertThat("Should contain single filter", query.bool().filter(), hasSize(1));

		final var filterQuery = query.bool().filter().get(0).terms();

		assertThat("Filter query should not be null",
			filterQuery, is(notNullValue()));

		assertThat("Filter query should be for subjects",
			filterQuery.field(), is("metadata.derivedType.keyword"));

		final var termValues = filterQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Filter query should include multiple terms",
			termValues, containsInAnyOrder("DVD", "Journal", "Book"));
	}

	@Test
	void shouldInterpretQueryWithMultipleFilterClauses() {
		final var query = interpretCql("""
			title all surface
			and instancePublishers==("Orbit" or "Penguin")
			and instanceSubjects==("Politics" or "Government")
			and sourceTypes==("Book")"
		""");

		assertThat("Query should not be null", query, is(notNullValue()));

		final var boolQuery = query.bool();

		assertThat("Should be a boolean query", boolQuery, is(notNullValue()));

		assertThat("Should contain one must query", boolQuery.must(), hasSize(1));
		assertThat("Should contain two filter queries", boolQuery.filter(), hasSize(3));

		final var titleQuery = boolQuery.must().get(0);

		assertThat("Search query should not be null",
			titleQuery.queryString(), is(notNullValue()));

		assertThat("Search query text should be title query",
			titleQuery.queryString().query(), is("title:surface"));

		final var publishersQuery = boolQuery.filter().get(0).terms();

		assertThat("Publishers filter query should not be null",
			publishersQuery, is(notNullValue()));

		assertThat("Filter query should be for publishers",
			publishersQuery.field(), is("publisher.keyword"));

		final var publisherTerms = publishersQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Publishers filter query should include multiple terms",
			publisherTerms, containsInAnyOrder("Orbit", "Penguin"));

		final var subjectsQuery = boolQuery.filter().get(2).terms();

		assertThat("Subjects filter query should not be null",
			subjectsQuery, is(notNullValue()));

		assertThat("Filter query should be for subjects",
			subjectsQuery.field(), is("metadata.subjects.label.keyword"));

		final var subjectTermValues = subjectsQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Subjects filter query should include multiple terms",
			subjectTermValues, containsInAnyOrder("Politics", "Government"));

		final var formatsQuery = boolQuery.filter().get(1).terms();

		assertThat("Formats filter query should not be null",
			formatsQuery, is(notNullValue()));

		assertThat("Filter query should be for formats",
			formatsQuery.field(), is("metadata.derivedType.keyword"));

		final var formatTermValues = formatsQuery.terms().value().stream()
			.map(FieldValue::stringValue)
			.toList();

		assertThat("Format filter query should include single term",
			formatTermValues, containsInAnyOrder("Book"));
	}

	@ParameterizedTest
	@CsvSource({
		"title all \"guards!\",guards\\!",
		"title all \"guards-\",guards\\-",
		"title all \"+science\",\\+science",
		"title all \"food &&\",food \\&&",
		"title all \"food ||\",food \\||",
		"title all \"science (biology)\",science \\(biology\\)",
		"title all \"gems { amethyst }\",gems \\{ amethyst \\}",
		"title all \"terry [pratchett]\",terry \\[pratchett\\]",
		"title all \"pie^\",pie\\^",
		"title all \"food = pie\",food \\= pie",
		"title all \"some \\\"quoted\\\" term\",some \\\"quoted\\\" term",
		"title all \"science~\",science\\~",
		"title all \"who?\",who\\?",
		"title all \"star wars: a new hope\",star wars\\: a new hope",
		"title all \"foo / bar\",foo \\/ bar"
	})
	void specialCharactersShouldBeEscapedInQueryStringQuery(String queryText, String expectedEscapedQuery) {
		final var query = interpretCql(queryText);

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be CQL term only",
			query.queryString().query(), is("title:" + expectedEscapedQuery));
	}

	@Test
	void shouldNotEscapeBackslashSpecialCharacterBecauseLocateAlreadyEscapesIt() {
		final var query = interpretCql("title all \"foo \\\\ bar\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be CQL term only",
			query.queryString().query(), is("title:foo \\\\ bar"));
	}

	@Test
	void shouldNotEscapeAsteriskSpecialCharacter() {
		final var query = interpretCql("title all \"terry*\"");

		assertThat("Query should not be null", query, is(notNullValue()));

		assertThat("Should be a query string query",
			query.queryString(), is(notNullValue()));

		assertThat("Query text should be CQL term only",
			query.queryString().query(), is("title:terry*"));
	}

	@Test
	void shouldFailWhenQueryIncludesMultipleIndexSortSpecification() {
		final var cql = """
				title all "Guards"
				sortBy title/sort.ascending publication
				contributors/sort.ascending
			""";

		final var exception = assertThrows(UnexpectedClauseException.class,
			() -> interpretCql(cql));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unexpected clause in CQL: \"" + cql + "\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldFailWhenSortSpecificationContainsUnrecognisedIndex() {
		final var cql = "title all \"Guards\" sortBy subjects";

		final var exception = assertThrows(UnrecognisedCqlIndexException.class,
			() -> interpretCql(cql));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unrecognised CQL index \"subjects\" in query: \"" + cql + "\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldFailWhenSortSpecificationContainsUnrecognisedModifier() {
		final var cql = "title all \"Guards\" sortBy title/foo";

		final var exception = assertThrows(UnexpectedClauseException.class,
			() -> interpretCql(cql));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unexpected clause in CQL: \"" + cql + "\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldFailWhenQueryIncludesUnexpectedClauses() {
		final var exception = assertThrows(UnexpectedClauseException.class,
			() -> interpretCql("title all prisoners or title all science"));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message", exception.getMessage(), is(
			"Unexpected clause in CQL: \"title all prisoners or title all science\""));

		assertThat("Exception cause should be null", exception.getCause(), is(nullValue()));
	}

	private Query interpretCql(String cql) {
		return cqlInterpreter.interpretCql(cql).getQuery();
	}
}
