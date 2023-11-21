package org.dcb.locate.model.cql;

import static org.dcb.locate.model.cql.Matchers.isBooleanOrClause;
import static org.dcb.locate.model.cql.Matchers.isTermClause;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Set;

import org.junit.jupiter.api.Test;

class BooleanOrClauseTests {
	@Test
	void shouldNotFlattenTermNodes() {
		final var clause = BooleanOrClause.builder()
			.clauses(Set.of(
				termClause("Penguin"),
				termClause("Orbit")
			))
			.build();

		assertThat(clause.flatten(""), isBooleanOrClause(containsInAnyOrder(
			isTermClause("instancePublishers", "==", "Penguin"),
			isTermClause("instancePublishers", "==", "Orbit")
		)));
	}
	@Test
	void shouldFlattenSingleNestedOrClause() {
		final var clause = BooleanOrClause.builder()
			.clauses(Set.of(
				termClause("Penguin"),
				BooleanOrClause.builder()
					.clauses(Set.of(
						termClause("HarperCollins"),
						termClause("Orbit")
					))
					.build()
			))
			.build();

		assertThat(clause.flatten(""), isBooleanOrClause(containsInAnyOrder(
			isTermClause("instancePublishers", "==", "Penguin"),
			isTermClause("instancePublishers", "==", "Orbit"),
			isTermClause("instancePublishers", "==", "HarperCollins")
		)));
	}

	private static TermClause termClause(String term) {
		return TermClause.builder()
			.index("instancePublishers")
			.relation("==")
			.term(term)
			.build();
	}
}
