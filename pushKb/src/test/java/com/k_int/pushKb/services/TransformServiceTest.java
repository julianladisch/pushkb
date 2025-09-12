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
import com.k_int.pushKb.interactions.gokb.services.GokbFeedService;
import com.k_int.pushKb.model.*;
import com.k_int.pushKb.transform.model.ProteusSpecSource;
import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.model.Transform;
import com.k_int.pushKb.transform.services.TransformService;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.server.TestEmbeddedServer;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
 Tests the TransformService:
 - Based on some test JSON from the gokb source (resources/preTransformPkgs.json), do we get the expected output (postTransformPkgs.json) when
 capturing the output from the push()?
 */
@Slf4j
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
public class TransformServiceTest {

	DestinationService destinationService;
	PushSessionDatabaseService pushSessionDatabaseService;
	PushChunkDatabaseService pushChunkDatabaseService;
	TestEmbeddedServer embeddedServer;
	String accessibleUrl;
	private final static String PKG_TRANSFORM_FILE = "GOKBScroll_PKG_ERM_transformV1.json";

	@Inject
	FileLoaderService fileLoaderService;

	@Inject
	GokbFeedService gokbFeedService;

	@Inject
	ObjectMapper objectMapper;

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

	@BeforeEach
	void setupMocks() {
		destinationService = mock(DestinationService.class);
		pushSessionDatabaseService = mock(PushSessionDatabaseService.class);
		pushChunkDatabaseService = mock(PushChunkDatabaseService.class);
		embeddedServer = mock(TestEmbeddedServer.class);
		accessibleUrl = "test_accessible.url.org";
	}

	@Test
	void testTransformsPkgToPkg() throws IOException {
		String PKG_TRANSFORM_NAME = "GOKb_Package_to_Pkg_V1";
		final UUID sourceId = UUID.randomUUID();
		final UUID sourceTippId = UUID.randomUUID();
		final UUID gokbId = UUID.randomUUID();
		final UUID gokbTippId = UUID.randomUUID();
		final UUID destinationId = UUID.randomUUID();
		final UUID tenantId = UUID.randomUUID();

		Gokb gokb = Gokb.builder()
			.id(gokbId)
			.baseUrl("https://gokbt.gbv.de")
			.name("GOKB")
			.build();

		GokbSource source =  GokbSource.builder()
			.gokb(gokb)
			.id(sourceId)
			.name("GOKB_PKG")
			.gokbSourceType(GokbSourceType.PACKAGE)
			.build();

		Gokb gokbTIPP = Gokb.builder()
			.id(gokbTippId)
			.baseUrl("https://gokbt.gbv.de")
			.name("GOKB")
			.build();

		GokbSource sourceTIPP =  GokbSource.builder()
			.gokb(gokbTIPP)
			.id(sourceTippId)
			.name("GOKB_TIPP")
			.gokbSourceType(GokbSourceType.TIPP)
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

		JsonNode pkgSpec = fileLoaderService.readJsonFile(PKG_TRANSFORM_FILE, FileLoaderService.TRANSFORM_SPEC_PATH);

		ProteusTransform pkgTransform = ProteusTransform.builder()
			.id(Transform.generateUUID(PKG_TRANSFORM_NAME))
			.source(ProteusSpecSource.STRING_SPEC)
			.slug(PKG_TRANSFORM_NAME)
			.name(PKG_TRANSFORM_NAME)
			.spec(pkgSpec)
			.build();

		// Save all of our test data to the db.
		Mono.from(gokbDatabaseService.save(gokb)).block();
		Mono.from(gokbDatabaseService.save(gokbTIPP)).block();
		Mono.from(sourceService.ensureSource(source)).cache().block();
		Mono.from(sourceService.ensureSource(sourceTIPP)).cache().block();
		Mono.from(folioTenantDatabaseService.ensureFolioTenant(tenant)).block();
		Mono.from(folioDestinationDatabaseService.save(destination)).cache().block();
		Mono.from(transformService.saveOrUpdate(pkgTransform.getClass(), pkgTransform)).cache().block();

		// The pushable needs genuine references to an existing source, destination, transform for all the repository methods to work.
		PushTask initialPushable = PushTask.builder()
			.id(UUID.randomUUID())
			.transformId(pkgTransform.getId())
			.transformType(pkgTransform.getClass())
			.sourceId(source.getId())
			.sourceType(GokbSource.class)
			.destinationType(FolioDestination.class)
			.destinationId(destinationId)
			.footPointer(Instant.EPOCH) // These pointer dates will be re-assigned later on by the runPushable() method anyway.
			.destinationHeadPointer(Instant.EPOCH)
			.build();

		// Configure the mocks to control the recursion
		// Mocks for objects
		DestinationClient<Destination> mockClient = mock(DestinationClient.class);
		PushChunk mockChunk = mock(PushChunk.class);
		PushSession mockSession = mock(PushSession.class);
		UUID sessionId = UUID.randomUUID();

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

		PushService pushService = new PushService(
			accessibleUrl,
			sourceRecordDatabaseService,
			pushableService,
			destinationService,
			pushSessionDatabaseService,
			pushChunkDatabaseService,
			embeddedServer,
			transformService
		);

		// The goal here is to load in the test packages as JSON and save these as source records (using saveSourceRecordsFromPage).
		// THEN processAndPushRecords should then be able to get those records by calling get1000SourceRecords
		// and transform them, then attempt to push them to the destination. We capture the pushed record JSON and check it against a source of truth.
		List<JsonNode> packageList;
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("preTransformPkgs.json")) {
			if (in == null) {
				throw new RuntimeException("Cannot find resource file 'preTransformPkgs.json'");
			}
			packageList = objectMapper.readValue(in, Argument.listOf(JsonNode.class));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load or parse JSON", e);
		}

		// Save the source records based on the test packages we loaded in.
		Mono.from(gokbFeedService.saveSourceRecordsFromPage(packageList, source)).cache().block();

		// Act: Call the runPushable method to run runPushableRecursive()
		Mono<Pushable> resultMono = Mono.from(pushService.runPushable(initialPushable));

		// Use StepVerifier to check runPushable method ran.
		StepVerifier.create(resultMono)
			.expectNextCount(1)
			.verifyComplete();

		// ArgumentCaptor can be used to capture the data passed to a mocked service method call, so we use it to capture
		// the "json" sent in the .push() method.
		ArgumentCaptor<JsonNode> jsonCaptor = ArgumentCaptor.forClass(JsonNode.class);

		// Verify that the destination service's `push` method was called once.
		verify(destinationService, times(1)).push(
			any(),
			any(),
			jsonCaptor.capture()
		);

		// Get the captured JSON that would be pushed.
		JsonNode actualJson = jsonCaptor.getValue();
		JsonNode expectedJson;

		try (InputStream in = getClass().getResourceAsStream("/postTransformPkgs.json")) {
			expectedJson = objectMapper.readValue(in, JsonNode.class);
		}

		log.debug("Actual JSON: {}", objectMapper.writeValueAsString(actualJson.get("records")));
		log.debug("Expected JSON: {}", objectMapper.writeValueAsString(expectedJson));

		// Convert the expected ArrayNode to Sets of Json Nodes, which allows for comparison.
		Set<JsonNode> actualSet = StreamSupport.stream(actualJson.get("records").values().spliterator(), false)
			.collect(Collectors.toSet());

		Set<JsonNode> expectedSet = StreamSupport.stream(expectedJson.values().spliterator(), false)
			.collect(Collectors.toSet());

		// Perform the comparison. This will fail if any fields have different values.
		Assertions.assertEquals(expectedSet, actualSet);
	}
}
