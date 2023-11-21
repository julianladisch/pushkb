package org.dcb.locate.model.cql;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

class Matchers {
	@NotNull
	static Matcher<Clause> isTermClause(String expectedIndex,
		String expectedRelation, String expectedTerm) {

		return allOf(
			instanceOf(TermClause.class),
			hasProperty("index", is(expectedIndex)),
			hasProperty("relation", is(expectedRelation)),
			hasProperty("term", is(expectedTerm))
		);
	}

	@NotNull
	static Matcher<Clause> isBooleanOrClause(
		Matcher<Iterable<? extends Clause>> clausesMatcher) {

		return
			allOf(
				instanceOf(BooleanOrClause.class),
				hasProperty("clauses", clausesMatcher)
			);
	}

	@NotNull
	static Matcher<Clause> isBooleanAndClause(
		Matcher<Iterable<? extends Clause>> clausesMatcher) {

		return
			allOf(
				instanceOf(BooleanAndClause.class),
				hasProperty("clauses", clausesMatcher)
			);
	}

	@NotNull
	static Matcher<Clause> isSortSpecificationClause(String expectedSortIndex,
		String expectedSortOrder, Matcher<Clause> searchClauseMatcher) {

		return allOf(
			instanceOf(SortSpecificationClause.class),
			hasProperty("sortOptions", containsInAnyOrder(
				allOf(
					hasProperty("index", is(expectedSortIndex)),
					hasProperty("order", is(expectedSortOrder))
				)
			)),
			hasProperty("searchClause", searchClauseMatcher)
		);
	}
}
