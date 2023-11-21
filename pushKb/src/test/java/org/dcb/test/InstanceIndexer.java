package org.dcb.test;

import static co.elastic.clients.elasticsearch._types.mapping.DynamicMapping.Strict;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.dcb.test.fixtures.ElasticSearchFixture;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.util.ObjectBuilder;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.Builder;

@Singleton
public class InstanceIndexer {
	private final ElasticSearchFixture elasticSearchFixture;
	private final String instancesIndexName;

	public InstanceIndexer(ElasticSearchFixture elasticSearchFixture,
		@Value("${elasticsearch.indexes.instances}") String instancesIndexName) {

		this.instancesIndexName = instancesIndexName;
		this.elasticSearchFixture = elasticSearchFixture;
	}

	public void indexDocuments(InstanceDocument... documents) {
		indexDocuments(asList(documents));
	}

	public void indexDocuments(List<InstanceDocument> documents) {
		final var mappedDocuments = documents.stream()
			.map(InstanceIndexer::mapToExistingRepresentation)
			.toList();

		elasticSearchFixture.indexDocuments(instancesIndexName, mappedDocuments);
	}

	private static InternalInstanceDocument mapToExistingRepresentation(
		InstanceDocument instanceDocument) {

		return InternalInstanceDocument.builder()
			.bibClusterId(instanceDocument.getId().toString())
			.title(instanceDocument.getTitle())
			.isbn(instanceDocument.getIsbns())
			.issn(instanceDocument.getIssns())
			.publisher(instanceDocument.getPublisher())
			.dateOfPublication(instanceDocument.getPublicationDate())
			.yearOfPublication(instanceDocument.getPublicationYear())
			.primaryAuthor(firstAuthorsName(instanceDocument.getAuthors()))
			.metadata(Metadata.builder()
				.identifiers(mapIdentifiers(instanceDocument))
				.subjects(mapSubjects(instanceDocument.getSubjects()))
				.agents(mapToAgents(instanceDocument.getAuthors()))
				.derivedType(instanceDocument.getFormat())
				.physicalDescriptions(mapToPhysicalDescriptions(instanceDocument.getPhysicalDescriptions()))
				.build())
			.build();
	}

	@Nullable
	private static String firstAuthorsName(List<Author> authors) {
		return authors.stream()
			.map(Author::getName)
			.findFirst()
			.orElse(null);
	}

	private static List<Identifier> mapIdentifiers(InstanceDocument document) {
		return Stream.concat(
				mapIsbnIdentifiers(document),
				mapIssnIdentifiers(document))
			.toList();
	}

	@NotNull
	private static Stream<Identifier> mapIssnIdentifiers(InstanceDocument document) {
		return document.getIssns()
			.stream()
			.map(value -> Identifier.builder()
				.namespace("ISSN")
				.value(value)
				.build());
	}

	@NotNull
	private static Stream<Identifier> mapIsbnIdentifiers(InstanceDocument document) {
		return document.getIsbns()
			.stream()
			.map(value -> Identifier.builder()
				.namespace("ISBN")
				.value(value)
				.build());
	}

	private static List<Subject> mapSubjects(List<String> subjects) {
		return subjects.stream()
			.map(subject -> Subject.builder()
				.label(subject)
				.build())
			.toList();
	}

	private static List<Agent> mapToAgents(List<Author> authors) {
		return authors.stream()
			.map(author -> Agent.builder()
				.label(author.getName())
				.build())
			.toList();
	}

	private static List<PhysicalDescription> mapToPhysicalDescriptions(
		List<String> physicalDescriptions) {

		return physicalDescriptions.stream()
			.map(description -> PhysicalDescription.builder()
				.label(description)
				.build())
			.toList();
	}

	public void defineIndex() {
		elasticSearchFixture.recreateIndex(instancesIndexName);

		elasticSearchFixture.defineMappings(instancesIndexName,
			PutMappingRequest.of(mapping -> mapping
				.dynamic(Strict)
				.index(List.of(instancesIndexName))
				.properties("bibClusterId", InstanceIndexer::textProperty)
				.properties("title", InstanceIndexer::textProperty)
				.properties("issn", InstanceIndexer::textProperty)
				.properties("isbn", InstanceIndexer::textProperty)
				.properties("publisher", InstanceIndexer::textProperty)
				.properties("dateOfPublication", InstanceIndexer::textProperty)
				.properties("yearOfPublication", InstanceIndexer::longProperty)
				.properties("metadata.identifiers.namespace", InstanceIndexer::keywordProperty)
				.properties("metadata.identifiers.value", InstanceIndexer::keywordProperty)
				.properties("metadata.subjects.label", InstanceIndexer::textProperty)
				.properties("metadata.agents.label", InstanceIndexer::textProperty)
				.properties("metadata.derivedType", InstanceIndexer::textProperty)
				.properties("metadata.physical-description.label", InstanceIndexer::textProperty)
				.properties("primaryAuthor", InstanceIndexer::textProperty)
		));
	}

	private static ObjectBuilder<Property> textProperty(Property.Builder property) {
		return property.text(text -> text
			.index(true)
			.fields("keyword", Property.of(InstanceIndexer::keywordProperty))
		);
	}

	private static ObjectBuilder<Property> keywordProperty(Property.Builder property) {
		return property.keyword(keyword -> keyword.index(true));
	}

	private static ObjectBuilder<Property> longProperty(Property.Builder property) {
		return property.long_(number -> number.index(true));
	}

	@lombok.Value
	@Builder
	public static class InstanceDocument {
		@Builder.Default UUID id = UUID.randomUUID();
		String title;
		@Builder.Default List<String> isbns = emptyList();
		@Builder.Default List<String> issns = emptyList();
		@Builder.Default List<String> subjects = emptyList();
		@Builder.Default List<Author> authors = emptyList();
		String publisher;
		String publicationDate;
		Integer publicationYear;
		String format;
		@Builder.Default List<String> physicalDescriptions = emptyList();
	}

	@lombok.Value
	@Builder
	public static class Author {
		String name;
	}

	/**
	 * Represents the structure of the existing index created by a script outside of this project
	 */
	@lombok.Value
	@Builder
	@Serdeable
	private static class InternalInstanceDocument {
		String bibClusterId;
		String title;
		List<String> isbn;
		List<String> issn;
		String publisher;
		String primaryAuthor;
		String dateOfPublication;
		Integer yearOfPublication;
		Metadata metadata;
	}

	@lombok.Value
	@Builder
	@Serdeable
	private static class Metadata {
		List<Subject> subjects;
		List<Agent> agents;
		String derivedType;
		@JsonProperty("physical-description")
		List<PhysicalDescription> physicalDescriptions;
		List<Identifier> identifiers;
	}

	@lombok.Value
	@Builder
	@Serdeable
	private static class Subject {
		String label;
	}

	@lombok.Value
	@Builder
	@Serdeable
	private static class Agent {
		String label;
	}

	@lombok.Value
	@Builder
	@Serdeable
	private static class PhysicalDescription {
		String label;
	}

	@lombok.Value
	@Builder
	@Serdeable
	private static class Identifier {
		String namespace;
		String value;
	}
}
