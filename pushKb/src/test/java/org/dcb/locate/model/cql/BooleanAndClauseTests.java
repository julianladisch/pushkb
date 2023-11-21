package org.dcb.locate.model.cql;

import static org.dcb.locate.model.cql.Matchers.isBooleanAndClause;
import static org.dcb.locate.model.cql.Matchers.isTermClause;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Set;

import org.junit.jupiter.api.Test;

class BooleanAndClauseTests {
	@Test
	void shouldNotFlattenTermNodes() {
		final var clause = BooleanAndClause.builder()
			.clauses(Set.of(
				termClause("title", "all", "Guards"),
				termClause("contributors", "==", "Pratchett")
			))
			.build();

		assertThat(clause.flatten(), isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "Guards"),
			isTermClause("contributors", "==", "Pratchett")
		)));
	}

	@Test
	void shouldFlattenSingleNestedAndClause() {
		final var clause = BooleanAndClause.builder()
			.clauses(Set.of(
				termClause("title", "all", "Guards"),
				BooleanAndClause.builder()
					.clauses(Set.of(
						termClause("subjects", "==", "Fiction"),
						termClause("contributors", "==", "Pratchett")
					))
					.build()
			))
			.build();

		assertThat(clause.flatten(), isBooleanAndClause(containsInAnyOrder(
			isTermClause("title", "all", "Guards"),
			isTermClause("subjects", "==", "Fiction"),
			isTermClause("contributors", "==", "Pratchett")
		)));
	}

	private static TermClause termClause(String index, String relation, String term) {
		return TermClause.builder()
			.index(index)
			.relation(relation)
			.term(term)
			.build();
	}
}
