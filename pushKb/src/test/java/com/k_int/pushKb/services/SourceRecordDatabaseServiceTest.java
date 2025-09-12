package com.k_int.pushKb.services;

import com.k_int.pushKb.Application;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import io.micronaut.context.env.Environment;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/*
Tests the SourceRecordDatabaseService:
- Do we get the correct total record count?
- Do we fetch the correct source records within a specific date range?
- Do we get the correct source record count within a specific date range?
- When fetching records using the "forUpdate" method, called during a "catch-up" situation, do we find records for a specific date if the records exist, and none
when no records exist at that date?
 */
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
@Slf4j
class SourceRecordDatabaseServiceTest {

	@MockBean(ReactiveDutyCycleTaskRunner.class)
	ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner() { return Mockito.mock(ReactiveDutyCycleTaskRunner.class); }

	@Inject
	SourceRecordDatabaseService databaseService;

	SourceRecord record;

	final UUID sourceSystemId = UUID.randomUUID();
	final UUID recordId = UUID.randomUUID();

	@BeforeEach
	void resetDatabase() {
		// Deletes all existing source records and then creates a new single record.
		Mono.from(databaseService.deleteAll()).block();

		Instant startTime = Instant.parse("2023-10-27T10:00:00Z");
		record = SourceRecord.builder()
			.id(recordId)
			.sourceUUID(String.valueOf(sourceSystemId))
			.sourceId(sourceSystemId)
			.sourceType(GokbSource.class)
			.updated(startTime)
			.lastUpdatedAtSource(startTime)
			.jsonRecord(JsonNode.createObjectNode(Map.of(
				"record_number", JsonNode.createNumberNode(1),
				"status", JsonNode.createStringNode("new")
			)))
			.build();

		Mono.from(databaseService.saveOrUpdateRecord(record));
	}

	@Test
	void countRecordsShouldFindOneRecord() {
		// Testing countRecords(): Check the total record count is correct.
		Mono<Long> recordCount = Mono.from(databaseService.saveOrUpdateRecord(record))
			.then(Mono.from(databaseService.countRecords()));

		StepVerifier.create(recordCount)
			.expectNext(1L)
			.verifyComplete();
	}

	@Test
	void getSourceRecordFeedShouldFindOneRecord() {
		// Testing getSourceRecordFeed(): Check the record ID saved is correct.
		Mono<SourceRecord> recordResult = Mono.from(databaseService.saveOrUpdateRecord(record))
			.flatMap(savedRecord -> Mono.from(databaseService.getSourceRecordFeed(
				sourceSystemId,
				Instant.parse("2022-10-26T10:00:00Z"),
				Instant.parse("2026-10-28T10:00:00Z"),
				Optional.empty()
			)));

		StepVerifier.create(recordResult)
			.assertNext(retrievedRecord -> {
				Assertions.assertNotNull(retrievedRecord);
				Assertions.assertEquals(recordId, retrievedRecord.getId());
			})
			.verifyComplete();
	}

	@Test
	void countFeedShouldFindOneRecord() {
		// Testing countFeed(): Check the correct number of records are fetched based on updated field.
		Mono<Long> feedCount = Mono.from(databaseService.saveOrUpdateRecord(record))
			.flatMap(savedRecord -> Mono.from(databaseService.countFeed(sourceSystemId,
			Instant.parse("2022-10-26T10:00:00Z"),
			Instant.parse("2026-10-28T10:00:00Z"),
			Optional.empty())));

		StepVerifier.create(feedCount)
			.expectNext(1L)
			.verifyComplete();
	}

	@Test
	void getSourceRecordFeedForUpdatedShouldFindCorrectRecords() {
  // Testing getSourceRecordFeedForUpdated(): check that no records are returned when we pass an Instant where none were updated.
		Mono<SourceRecord> updatedResultEmpty = Mono.from(databaseService.saveOrUpdateRecord(record)).flatMap(savedRecord -> Mono.from(databaseService.getSourceRecordFeedForUpdated(
			sourceSystemId,
			Instant.parse("2022-10-26T10:00:00Z"),
			Optional.empty()
		)));

		Mono<SourceRecord> updatedResultFound = Mono.from(databaseService.saveOrUpdateRecord(record)).flatMap(savedRecord -> Mono.from(databaseService.getSourceRecordFeedForUpdated(
			sourceSystemId,
			savedRecord.getUpdated(),
			Optional.empty()
		)));

		StepVerifier.create(updatedResultEmpty)
			.verifyComplete(); // Stream finished without emitting anything, which is what we expect as no records were updated at the example Instant.

		// Testing getSourceRecordFeedForUpdated(): check that the correct record is returned when we call with the time it was updated at.
		StepVerifier.create(updatedResultFound)
			.assertNext(retrievedRecord -> {
				Assertions.assertNotNull(retrievedRecord);
				Assertions.assertEquals(recordId, retrievedRecord.getId());
			})
			.verifyComplete();
	}
}
