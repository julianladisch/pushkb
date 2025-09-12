package com.k_int.pushKb.services;

import com.k_int.pushKb.Application;
import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioDestinationType;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.interactions.folio.services.FolioDestinationDatabaseService;
import com.k_int.pushKb.interactions.folio.services.FolioTenantDatabaseService;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.interactions.gokb.services.GokbDatabaseService;
import com.k_int.pushKb.model.*;
import com.k_int.pushKb.transform.model.ProteusSpecSource;
import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.model.Transform;
import com.k_int.pushKb.transform.services.TransformService;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.server.TestEmbeddedServer;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
Tests the PushService class:
- After calling runPushable on a set of test source records, are the pointers on the pushable updated correctly, and do we push the expected number of times?
- If there is a "catch-up" situation, do we push the expected number of times?
 */
@Slf4j
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = false)
public class PushServiceTest {

	DestinationService destinationService;
	PushSessionDatabaseService pushSessionDatabaseService;
	PushChunkDatabaseService pushChunkDatabaseService;
	TestEmbeddedServer embeddedServer;
	String accessibleUrl;

	@Inject
	R2dbcOperations r2dbcOperations;

	@Inject
	GokbDatabaseService gokbDatabaseService;

	@Inject
	SourceService sourceService;

	@Inject
	SourceRecordDatabaseService sourceRecordDatabaseService;

	@Inject
	PushableService pushableService;

	@Inject
	FolioTenantDatabaseService folioTenantDatabaseService;

	@Inject
	FolioDestinationDatabaseService folioDestinationDatabaseService;

	@Inject
	TransformService transformService;

	@Inject
	ApplicationContext appContext;

	private final static String PKG_TRANSFORM_NAME = "GOKb_Package_to_Pkg_V1";

	@MockBean(ReactiveDutyCycleTaskRunner.class)
	ReactiveDutyCycleTaskRunner mockReactiveDutyCycleTaskRunner() {
		ReactiveDutyCycleTaskRunner mock = Mockito.mock(ReactiveDutyCycleTaskRunner.class);
		DutyCycleTask mockTask = Mockito.mock(DutyCycleTask.class);

		when(mock.registerTask(
			any(Long.class),
			anyString(),
			anyString(),
			any(Map.class)
		)).thenReturn(Mono.just(mockTask));

		return mock;
	}

	// Instantiate all the things
	final UUID sourceId = UUID.randomUUID();
	final UUID gokbId = UUID.randomUUID();
	final UUID destinationId = UUID.randomUUID();
	final UUID tenantId = UUID.randomUUID();

	Gokb gokb = Gokb.builder()
		.id(gokbId)
		.baseUrl("testURL.org")
		.name("testGokb")
		.build();

	GokbSource source =  GokbSource.builder()
		.gokb(gokb)
		.id(sourceId)
		.name("testSrc")
		.gokbSourceType(GokbSourceType.PACKAGE)
		.build();

	FolioTenant tenant = FolioTenant.builder()
		.id(tenantId)
		.authType(FolioAuthType.NONE)
		.baseUrl("")
		.tenant("")
		.name("")
		.loginUser("")
		.loginPassword("")
		.build();

	FolioDestination destination = FolioDestination.builder()
		.folioTenant(tenant)
		.name("")
		.id(destinationId)
		.destinationType(FolioDestinationType.PACKAGE)
		.build();

	ProteusTransform pkgTransform = ProteusTransform.builder()
		.id(Transform.generateUUID(PKG_TRANSFORM_NAME))
		.source(ProteusSpecSource.STRING_SPEC)
		.slug(PKG_TRANSFORM_NAME)
		.name(PKG_TRANSFORM_NAME)
		.spec(JsonNode.from(""))
		.build();

	// The pushable needs genuine references to an existing source, destination, transform for all the repository methods to work.
	PushTask initialPushable = PushTask.builder()
		.id(UUID.randomUUID())
		.transformId(pkgTransform.getId())
		.transformType(pkgTransform.getClass())
		.sourceId(sourceId)
		.sourceType(GokbSource.class)
		.destinationType(FolioDestination.class)
		.destinationId(destinationId)
		.footPointer(Instant.EPOCH) // These pointer dates will (/SHOULD) be sorted out later on by the runPushable() methods
		.destinationHeadPointer(Instant.EPOCH)
		.build();

	// Mocks for objects
	DestinationClient<Destination> mockClient = mock(DestinationClient.class);
	PushChunk mockChunk = mock(PushChunk.class);
	PushSession mockSession = mock(PushSession.class);

	UUID sessionId = UUID.randomUUID();
	Instant startTime = Instant.parse("2023-10-27T10:00:00Z"); // Arbitrary start date for our records chunk.

	PushService pushService;
	PushService pushServiceSpy;

	@BeforeEach
	void setupMocks() {
		destinationService = mock(DestinationService.class);
		pushSessionDatabaseService = mock(PushSessionDatabaseService.class);
		pushChunkDatabaseService = mock(PushChunkDatabaseService.class);
		embeddedServer = mock(TestEmbeddedServer.class);
		accessibleUrl = "test_accessible.url.org";

		log.info("initial pushable: {}", initialPushable);

		// Save all of our test data to the db.
		Mono.from(gokbDatabaseService.save(gokb)).block();
		Mono.from(sourceService.save(source)).cache().block();
		Mono.from(folioTenantDatabaseService.ensureFolioTenant(tenant)).block();
		Mono.from(folioDestinationDatabaseService.save(destination)).cache().block();
		Mono.from(transformService.saveOrUpdate(pkgTransform.getClass(), pkgTransform)).cache().block();

		// Mocks: Because we aren't fetching from an actual source or pushing to a real destination we can mock these classes and other unnecessary classes.
		when(mockSession.getId()).thenReturn(sessionId);
		when(mockChunk.getId()).thenReturn(sessionId);
		when(destinationService.getClient(any(Destination.class)))
			.thenAnswer(ans -> Mono.just(mockClient));
		when(pushSessionDatabaseService.save(any(PushSession.class)))
			.thenReturn(Mono.just(mockSession));
		when(pushChunkDatabaseService.save(any(PushChunk.class)))
			.thenReturn(Mono.just(mockChunk));
		when(destinationService.push(any(), any(), any(JsonNode.class)))
			.thenReturn(Mono.just(true));

		pushService = new PushService(
			accessibleUrl,
			sourceRecordDatabaseService,
			pushableService,
			destinationService,
			pushSessionDatabaseService,
			pushChunkDatabaseService,
			embeddedServer,
			transformService
		);
		pushServiceSpy = Mockito.spy(pushService);
	}

	@Test
	void testRunPushable() {
		// Generate 1500 mock records. Increment their updated time by 1s for each record, so updated times are in range 2023-10-27T10:00:01Z - 2023-10-27T10:25:00Z
		List<SourceRecord> mockRecords = IntStream.rangeClosed(1, 1500)
			.mapToObj(i ->
				SourceRecord.builder()
					.id(UUID.randomUUID())
					.sourceUUID("remote-record-" + i)
					.sourceId(sourceId)
					.sourceType(GokbSource.class)
					.updated(startTime.plusSeconds(i))
					.lastUpdatedAtSource(startTime.plusSeconds(i))
					.jsonRecord(JsonNode.createObjectNode(Map.of(
						"record_number", JsonNode.createNumberNode(i),
						"status", JsonNode.createStringNode("new")
					)))
					.build()
			)
			.collect(Collectors.toList());

		mockRecords.forEach(record -> {
			Instant currentUpdated = record.getUpdated();
			Mono.from(sourceRecordDatabaseService.saveOrUpdateRecord(record)).cache().block();

			// R2DBC workaround to Set the "updated" timestamps on the source records
			Mono.from(r2dbcOperations.withConnection(connection ->
				Mono.from(connection.createStatement("UPDATE source_record SET updated = $1 WHERE id = $2")
						.bind("$1", java.sql.Timestamp.from(currentUpdated))
						.bind("$2", record.getId())
						.execute())
					.flatMap(result -> Mono.from(result.getRowsUpdated()))
			)).block();
		});

		// Act: Call the runPushable method() to run runPushableRecursive()
		Mono<Pushable> testPipeline = pushService.runPushable(initialPushable);

		// Assert:
		// Has the foot pointer moved up to the "latest seen" record updated time in our block of 1500 records?
		StepVerifier.create(testPipeline)
			.assertNext(finalPushable -> {
				Assertions.assertEquals(Instant.parse("2023-10-27T10:25:00Z"), finalPushable.getFootPointer());
			})
			.verifyComplete();

		// With 1500 records, we should push to the destination twice (if processing 1000 records each time).
		verify(destinationService, times(2)).push(any(), any(), any());
	}

	// Also test catch up: create 4500 records with 9 additional records all with the same updated date at the 'bottom' of the first chunk.
	@Test
	void testRunPushableWithCatchup() {
		// Create and save mock records: The records should span the updated times 2023-10-27T10:00:01Z - 2023-10-27T11:15:00Z
		List<SourceRecord> mockRecords = IntStream.rangeClosed(1, 4500)
			.mapToObj(i ->
				SourceRecord.builder()
					.id(UUID.randomUUID())
					.sourceUUID("remote-record-" + i)
					.sourceId(sourceId)
					.sourceType(GokbSource.class)
					.updated(i > 3495 && i < 3505 ? startTime.plusSeconds(3496) : startTime.plusSeconds(i)) // Make it so that the first six records have the same updated time.
					.lastUpdatedAtSource(startTime.plusSeconds(i))
					.jsonRecord(JsonNode.createObjectNode(Map.of(
						"record_number", JsonNode.createNumberNode(i),
						"status", JsonNode.createStringNode("new")
					)))
					.build()
			)
			.collect(Collectors.toList());

		mockRecords.forEach(record -> {
			Instant currentUpdated = record.getUpdated();
			Mono.from(sourceRecordDatabaseService.saveOrUpdateRecord(record)).cache().block();

			// R2DBC workaround to Set the "updated" timestamps on the source records
			Mono.from(r2dbcOperations.withConnection(connection ->
				Mono.from(connection.createStatement("UPDATE source_record SET updated = $1 WHERE id = $2")
						.bind("$1", java.sql.Timestamp.from(currentUpdated))
						.bind("$2", record.getId())
						.execute())
					.flatMap(result -> Mono.from(result.getRowsUpdated()))
			)).block();
		});

		// Act: Call the runPushable method() to run runPushableRecursive()
		Mono<Pushable> testPipeline = pushServiceSpy.runPushable(initialPushable);

		// Assert: Has the foot pointer moved up to the "latest seen" record updated time in our block of 1000 records?
		StepVerifier.create(testPipeline)
			.assertNext(finalPushable -> {
				Assertions.assertEquals(Instant.parse("2023-10-27T11:15:00Z"), finalPushable.getFootPointer());
			})
			.verifyComplete();

		// Should be 6 pushes inc. catchup: (1) 4500 -> 3501, (2) catch-up, (3) 3495 -> 2496, (4) 2495 -> 1496, (5) 1495 -> 496, (6) 495 -> 0
		verify(destinationService, times(6)).push(any(), any(), any());

		// ensure getCatchUpSourceRecords is called. We could go further here and check the number of missing records, but this is probably sufficient.
		verify(pushServiceSpy, times(1)).getCatchUpSourceRecords(any(), any());
	}

}
