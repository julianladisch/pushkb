package org.dcb.locate.model.cql;

import static org.dcb.locate.model.cql.Matchers.isBooleanAndClause;
import static org.dcb.locate.model.cql.Matchers.isBooleanOrClause;
import static org.dcb.locate.model.cql.Matchers.isSortSpecificationClause;
import static org.dcb.locate.model.cql.Matchers.isTermClause;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class CqlParserTests {
	private final CqlParser parser = new CqlParser();

	@ParameterizedTest
	@CsvSource({
		"@keyword all \"science\", @keyword, all, science",
		"title all \"science\", title, all, science",
		"contributors all \"pratchett\", contributors, all, pratchett",
		"subjects all \"anthropology\", subjects, all, anthropology",
		"issn = \"0036-8733\", issn, =, 0036-8733",
		"isbn = \"9780061020643\", isbn, =, 9780061020643",
		"cql.allRecords = 1, cql.allRecords, =, 1"
	})
	void shouldParseTermClauseOnly(String cql, String expectedIndex,
		String expectedRelation, String expectedTerm) {

		final var tree = parser.parse(cql);

		assertThat(tree, isTermClause(expectedIndex, expectedRelation, expectedTerm));
	}

	@Test
	void shouldParseSingleIndexAscendingSortSpecification() {
		final var tree = parser.parse("title all \"Guards\" sortBy title/sort.ascending");

		assertThat(tree, isSortSpecificationClause("title", "sort.ascending",
			isTermClause("title", "all", "Guards")));
	}

	@Test
	void shouldParseSingleIndexDescendingSortSpecification() {
		final var tree = parser.parse("title all \"Guards\" sortBy title/sort.descending");

		assertThat(tree, isSortSpecificationClause("title", "sort.descending",
			isTermClause("title", "all", "Guards")));
	}

	@Test
	void shouldTolerateMissingSortOrderModifier() {
		final var tree = parser.parse("title all \"Guards\" sortBy title");

		assertThat(tree, isSortSpecificationClause("title", null,
			isTermClause("title", "all", "Guards")));
	}

	@Test
	void shouldParseTwoTermBooleanOrClauseOnly() {
		final var tree = parser.parse("instanceSubjects == (food or politics)");

		assertThat(tree, isBooleanOrClause(containsInAnyOrder(
			isTermClause("instanceSubjects", "==", "food"),
			isTermClause("instanceSubjects", "==", "politics")
		)));
	}

	@Test
	void shouldParseManyTermBooleanOrClauseOnly() {
		final var tree = parser.parse("instanceSubjects == (food or politics or anthropology or science or government)");

		assertThat(tree, isBooleanOrClause(containsInAnyOrder(
			isTermClause("instanceSubjects", "==", "anthropology"),
			isTermClause("instanceSubjects", "==", "politics"),
			isTermClause("instanceSubjects", "==", "food"),
			isTermClause("instanceSubjects", "==", "science"),
			isTermClause("instanceSubjects", "==", "government")
		)));
	}

	@Test
	void shouldParseTwoTermBooleanAndClauseOnly() {
		final var tree = parser.parse("title all food and instanceSubjects == science");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "food"),
			isTermClause("instanceSubjects", "==", "science")
		)));
	}

	@Test
	void shouldParseManyTermBooleanAndClauseOnly() {
		final var tree = parser.parse("""
			title all food
			and instanceSubjects == science
			and instancePublishers == Penguin
			""");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("instancePublishers", "==", "Penguin"),
			isTermClause("title", "all", "food"),
			isTermClause("instanceSubjects", "==", "science")
		)));
	}

	@Test
	void shouldParseCqlContainingSearchClauseAndSingleValueFilterClause() {
		final var tree = parser.parse("title all prisoners and instanceSubjects==(\"Politics\")");

		assertThat("Parsed CQL should not be null", tree, is(notNullValue()));

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "prisoners"),
			isTermClause("instanceSubjects", "==", "Politics")
		)));
	}

	@Test
	void shouldParseCqlContainingSearchClauseAndMultipleValueFilterClause() {
		final var tree = parser.parse(
			"title all gold and instanceSubjects==(\"History\" or \"United States\")");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "gold"),
			isBooleanOrClause(containsInAnyOrder(
				isTermClause("instanceSubjects", "==", "History"),
				isTermClause("instanceSubjects", "==", "United States")
			))
		)));
	}

	@Test
	void shouldParseCqlContainingSearchClauseAndManyValueFilterClause() {
		final var tree = parser.parse(
			"title all gold and instanceSubjects==(\"History\" or \"United States\" or \"Politics\" or \"Law\")");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "gold"),
			isBooleanOrClause(containsInAnyOrder(
				isTermClause("instanceSubjects", "==", "History"),
				isTermClause("instanceSubjects", "==", "United States"),
				isTermClause("instanceSubjects", "==", "Politics"),
				isTermClause("instanceSubjects", "==", "Law")
			)))));
	}

	@Test
	void shouldParseCqlContainingSearchClauseAndMultipleFilterClauses() {
		final var tree = parser.parse("""
			title all prisoners
			and instancePublishers==("BBC")
			and instanceSubjects==("Politics")
		""");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "prisoners"),
			isTermClause("instanceSubjects", "==", "Politics"),
			isTermClause("instancePublishers", "==", "BBC")
		)));
	}

	@Test
	void shouldParseCqlContainingMultipleFilterClausesContainingMultipleValues() {
		final var tree = parser.parse("""
			title all prisoners
			and instancePublishers==("BBC" or "Scientific American")
			and instanceSubjects==("Politics" or "Government")
		""");

		assertThat(tree, isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "prisoners"),
			isBooleanOrClause(containsInAnyOrder(
				isTermClause("instanceSubjects", "==", "Politics"),
				isTermClause("instanceSubjects", "==", "Government")
			)),
			isBooleanOrClause(containsInAnyOrder(
				isTermClause("instancePublishers", "==", "BBC"),
				isTermClause("instancePublishers", "==", "Scientific American")
			))
		)));
	}

	@Test
	void shouldParseCqlContainingOrBetweenDifferentIndexes() {
		final var tree = parser.parse("""
			instancePublishers=="BBC"
			or instanceSubjects=="Politics"
		""");

		assertThat(tree, isBooleanOrClause(containsInAnyOrder(
			isTermClause("instanceSubjects", "==", "Politics"),
			isTermClause("instancePublishers", "==", "BBC")
		)));
	}

	@Test
	void shouldParseCqlQueryContainingQuotes() {
		final var tree = parser.parse("title all \"some \\\"quoted\\\" term\"");

		assertThat(tree, isTermClause("title", "all", "some \"quoted\" term"));
	}

	@Test
	void shouldRejectCqlOrClauseContainingAndClause() {
		final var cql = """
				instancePublishers=="BBC"
				or (instanceSubjects=="Politics" and title=="Science")"
			""";

		final var exception = assertThrows(UnexpectedClauseException.class,
			() -> parser.parse(cql));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unexpected clause in CQL: \"" + cql + "\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldRejectCqlContainingUnexpectedClause() {
		final var cql = "cql.allRecords=1 NOT title all \"Science\"";

		final var exception = assertThrows(UnexpectedClauseException.class,
			() -> parser.parse(cql));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Unexpected clause in CQL: \"" + cql + "\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(nullValue()));
	}

	@Test
	void shouldRejectInvalidCql() {
		final var exception = assertThrows(FailedToParseCqlException.class,
			() -> parser.parse(""));

		assertThat("Exception should not be null", exception, is(notNullValue()));

		assertThat("Exception should have a message",
			exception.getMessage(), is("Failed to parse cql: \"\""));

		assertThat("Exception cause should not be null", exception.getCause(), is(notNullValue()));
	}
}
