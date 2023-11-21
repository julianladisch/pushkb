package org.dcb.test.matchers;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.stream.Stream;

import org.dcb.test.clients.SearchClient;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

public class InstanceMatchers {
	public static Matcher<SearchClient.Instance> hasTitle(String title) {
		return hasProperty("title", is(title));
	}

	public static Matcher<SearchClient.Instance> hasTitle() {
		return hasProperty("title", is(notNullValue()));
	}

	public static Matcher<SearchClient.Instance> hasNoTitle() {
		return hasProperty("title", is(nullValue()));
	}

	public static Matcher<SearchClient.Instance> hasIsbns(String... expectedValues) {
		return hasProperty("isbns", containsInAnyOrder(expectedValues));
	}

	public static Matcher<SearchClient.Instance> hasNoIsbns() {
		return hasProperty("isbns", is(nullValue()));
	}

	public static Matcher<SearchClient.Instance> hasIssns(String... expectedValues) {
		return hasProperty("issns", containsInAnyOrder(expectedValues));
	}

	public static Matcher<SearchClient.Instance> hasNoIssn() {
		return hasProperty("issns", is(nullValue()));
	}

	public static Matcher<SearchClient.Instance> hasIsbnIdentifiers(String... expectedValues) {
		final var identifierMatchers = Stream.of(expectedValues)
			.map(InstanceMatchers::hasIsbnIdentifier)
			.toList();

		return hasProperty("identifiers", containsInAnyOrder(identifierMatchers));
	}

	private static Matcher<Object> hasIsbnIdentifier(String expectedValue) {
		return hasIdentifier("8261054f-be78-422d-bd51-4ed9f33c3422", expectedValue);
	}

	public static Matcher<SearchClient.Instance> hasIssnIdentifiers(String... expectedValues) {
		final var identifierMatchers = Stream.of(expectedValues)
			.map(InstanceMatchers::hasIssnIdentifier)
			.toList();

		return hasProperty("identifiers", containsInAnyOrder(identifierMatchers));
	}

	private static Matcher<Object> hasIssnIdentifier(String expectedValue) {
		return hasIdentifier("913300b2-03ed-469a-8179-c1092c991227", expectedValue);
	}

	@NotNull
	private static Matcher<Object> hasIdentifier(String expectedType, String expectedValue) {
		return allOf(
			hasProperty("type", is(expectedType)),
			hasProperty("value", is(expectedValue))
		);
	}

	public static Matcher<SearchClient.Instance> hasSubjects(String... expectedSubjects) {
		return hasSubjects(Stream.of(expectedSubjects).toList());
	}

	public static Matcher<SearchClient.Instance> hasSubjects(List<String> expectedSubjects) {
		final var subjectMatchers = expectedSubjects.stream()
			.map(InstanceMatchers::hasSubject)
			.toList();

		return hasProperty("subjects", containsInAnyOrder(subjectMatchers));
	}

	private static Matcher<Object> hasSubject(String expectedSubject) {
		return hasProperty("value", is(expectedSubject));
	}

	public static Matcher<SearchClient.Instance> hasContributors(String... expectedNames) {
		return hasContributors(Stream.of(expectedNames).toList());
	}

	public static Matcher<SearchClient.Instance> hasContributors(List<String> expectedNames) {
		final var contributorMatchers = expectedNames.stream()
			.map(InstanceMatchers::hasName)
			.toList();

		return hasProperty("contributors", containsInAnyOrder(contributorMatchers));
	}

	public static Matcher<? super SearchClient.Instance> hasNoContributors() {
		return anyOf(
			hasProperty("contributors", is(nullValue())),
			hasProperty("contributors", everyItem(
				hasProperty("name", is(nullValue()))))
		);
	}

	private static Matcher<Object> hasName(String expectedName) {
		return hasProperty("name", is(expectedName));
	}

	public static Matcher<SearchClient.Instance> hasId() {
		return hasProperty("id", notNullValue());
	}

	public static Matcher<SearchClient.Instance> hasPublisher(String expectedPublisher) {
		return hasProperty("publication",
			containsInAnyOrder(hasProperty("publisher", is(expectedPublisher)))
		);
	}

	public static Matcher<SearchClient.Instance> hasNoPublisher() {
		return hasProperty("publication", is(nullValue()));
	}

	public static Matcher<? super SearchClient.Instance> hasPublicationDate(
		String expectedPublicationDate) {

		return allOf(
			hasProperty("publicationDate", is(expectedPublicationDate)),
				hasProperty("publication",
					containsInAnyOrder(hasProperty("dateOfPublication", is(expectedPublicationDate)))
		));
	}

	public static Matcher<? super SearchClient.Instance> hasNoPublicationDate() {
		return allOf(
			hasProperty("publicationDate", is(nullValue())),
			anyOf(
				hasProperty("publication", is(nullValue())),
				hasProperty("publication", everyItem(
					hasProperty("dateOfPublication", is(nullValue()))))
		));
	}

	public static Matcher<? super SearchClient.Instance> hasPublicationYear(
		Integer expectedPublicationYear) {

		return hasProperty("publicationYear", is(expectedPublicationYear));
	}

	public static Matcher<? super SearchClient.Instance> hasNoPublicationYear() {
		return hasProperty("publicationYear", is(nullValue()));
	}


	public static Matcher<SearchClient.Instance> hasSourceTypes(String... sourceTypes) {
		return hasProperty("sourceTypes", containsInAnyOrder(sourceTypes));
	}

	public static Matcher<? super SearchClient.Instance> hasNoSourceTypes() {
		return hasProperty("sourceTypes", is(nullValue()));
	}

	public static Matcher<SearchClient.Instance> hasNoPhysicalDescriptions() {
		return hasProperty("physicalDescriptions", nullValue());
	}

	public static Matcher<SearchClient.Instance> hasPhysicalDescriptions(
		String... expectedPhysicalDescriptions) {

		return hasProperty("physicalDescriptions",
			containsInAnyOrder(expectedPhysicalDescriptions));
	}

}
