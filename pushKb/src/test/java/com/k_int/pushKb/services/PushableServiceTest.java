package com.k_int.pushKb.services;

import com.k_int.pushKb.Application;
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
import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.transform.model.ProteusSpecSource;
import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.model.Transform;
import com.k_int.pushKb.transform.services.TransformService;

import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Property;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/* Based on PushableService, this test asks:
- Can we fetch the correct source and destination entities based on their ids?
- If we update a pushable, does it have the correct dates for the foot pointer, destination head pointer and last sent pointer?
- Do we register a task if we ensure a pushable?
 */
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
@Slf4j
public class PushableServiceTest {

//	 This prevents tasks from being started for the test.
	@MockBean(ReactiveDutyCycleTaskRunner.class)
	@Bean
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

//	ReactiveDutyCycleTaskRunner mockReactiveDutyCycleTaskRunner = Mockito.mock(ReactiveDutyCycleTaskRunner.class);

	@Inject // Inject the mock directly to ensure it's the one we're verifying against
	ReactiveDutyCycleTaskRunner injectedReactiveDutyCycleTaskRunner;

	@Inject
	PushableService pushableService;

	@Inject
	TransformService transformService;

	@Inject
	SourceService sourceService;

	@Inject
	FolioDestinationDatabaseService folioDestinationDatabaseService;

	@Inject
	GokbDatabaseService gokbDatabaseService;

	@Inject
	FolioTenantDatabaseService folioTenantDatabaseService;

	private final static String PKG_TRANSFORM_NAME = "GOKb_Package_to_Pkg_V1";

	final UUID expectedSourceId = UUID.randomUUID();
	final UUID expectedDestinationId = UUID.randomUUID();
	final UUID gokbId = UUID.randomUUID();
	final UUID tenantId = UUID.randomUUID();
	final UUID pushTaskId = UUID.randomUUID();

	Gokb gokb = Gokb.builder()
		.id(gokbId)
		.baseUrl("testURL.org")
		.name("testGokb")
		.build();

	GokbSource source =  GokbSource.builder()
		.gokb(gokb)
		.id(expectedSourceId)
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
		.id(expectedDestinationId)
		.destinationType(FolioDestinationType.PACKAGE)
		.build();

	Mono<Tuple3<Source, Destination, Transform>> pushableSetupData;
	ProteusTransform pkgTransform;

	@BeforeEach
	void setup() {
		pushableService = Mockito.spy(pushableService); // This is needed to run mockito.verify() on registerPushableTask() below.

		Mono.from(gokbDatabaseService.save(gokb)).block();
		Mono.from(folioTenantDatabaseService.ensureFolioTenant(tenant)).block();
		pkgTransform = ProteusTransform.builder()
			.id(Transform.generateUUID(PKG_TRANSFORM_NAME))
			.source(ProteusSpecSource.STRING_SPEC)
			.slug(PKG_TRANSFORM_NAME)
			.name(PKG_TRANSFORM_NAME)
			.spec(JsonNode.from(""))
 			.build();

		pushableSetupData = Mono.zip(
			Mono.from(sourceService.save(source)).cache(),
			Mono.from(folioDestinationDatabaseService.save(destination)).cache(),
			Mono.from(transformService.saveOrUpdate(pkgTransform.getClass(), pkgTransform)).cache()
		);
	}

	@Test
	void shouldFindSourceAndDestination() {
		// Here we're saying, we have a pushable that has the expected source, destination and transform- what happens when we
		// get the source and destination for the pushable?
		Mono<Tuple2<Source, Destination>> testPipeline = pushableSetupData
			.flatMap(savedEntitiesTuple -> {
				PushTask pushTaskObj = PushTask.builder()
					.id(pushTaskId)
					.transformId(pkgTransform.getId())
					.transformType(pkgTransform.getClass())
					.sourceType(GokbSource.class)
					.sourceId(expectedSourceId)
					.destinationType(FolioDestination.class)
					.destinationId(expectedDestinationId)
					.build();
				return Mono.from(pushableService.ensurePushable(pushTaskObj));
			})
			.flatMap(savedPushTask -> {
				return Mono.from(pushableService.getSourceAndDestination(savedPushTask));
			});

		// This runs the reactive code above, then we can assert against the resulting tuple.
		StepVerifier.create(testPipeline)
			.assertNext(resultTuple -> {
				Source retrievedSource = resultTuple.getT1();
				Destination retrievedDestination = resultTuple.getT2();

				Assertions.assertNotNull(retrievedSource);
				Assertions.assertNotNull(retrievedDestination);
				Assertions.assertEquals(expectedSourceId, retrievedSource.getId());
				Assertions.assertEquals(expectedDestinationId, retrievedDestination.getId());
			})
			.verifyComplete();
	}

	@Test
	void shouldUpdatePushablePointers() {
		Mono<Pushable> testPipeline = pushableSetupData
			.flatMap(savedEntitiesTuple -> {
				PushTask pushTaskObj = PushTask.builder()
					.id(pushTaskId)
					.transformId(pkgTransform.getId())
					.transformType(pkgTransform.getClass())
					.sourceType(GokbSource.class)
					.sourceId(expectedSourceId)
					.destinationType(FolioDestination.class)
					.destinationId(expectedDestinationId)
					.destinationHeadPointer(Instant.parse("2022-10-30T10:00:00Z"))
					.footPointer(Instant.parse("2022-10-26T10:00:00Z"))
					.lastSentPointer(Instant.parse("2022-10-27T10:00:00Z"))
					.build();
				return Mono.from(pushableService.ensurePushable(pushTaskObj));
			}).flatMap(psh -> {
				psh.setFootPointer(psh.getDestinationHeadPointer());
				psh.setLastSentPointer(Instant.parse("2022-10-28T10:00:00Z"));
				return Mono.from(pushableService.update(psh));
			});

		StepVerifier.create(testPipeline)
			.assertNext(resultTask -> {
				// Pointers have been updated correctly.
				Assertions.assertEquals(Instant.parse("2022-10-30T10:00:00Z"), resultTask.getFootPointer());
				Assertions.assertEquals(Instant.parse("2022-10-30T10:00:00Z"), resultTask.getDestinationHeadPointer());
				Assertions.assertEquals(Instant.parse("2022-10-28T10:00:00Z"), resultTask.getLastSentPointer());
			})
			.verifyComplete();
	}

	@Test
	void shouldRegisterTaskWhenEnsurePushable() {
		PushTask pushTaskObj = PushTask.builder()
			.id(pushTaskId) // This is a bit of a red herring, because the PushTaskDatabaseService will generate its own ID when ensuring a PushTask
			.transformId(pkgTransform.getId())
			.transformType(pkgTransform.getClass())
			.sourceType(GokbSource.class)
			.sourceId(expectedSourceId)
			.destinationType(FolioDestination.class)
			.destinationId(expectedDestinationId)
			.build();

		StepVerifier.create(pushableService.ensurePushable(pushTaskObj))
			.assertNext(result -> {
				Assertions.assertNotNull(result); // A pushable was created.
			})
			.verifyComplete();

		// We can then verify that registerPushableTask was called exactly once.
		verify(pushableService, times(1)).registerPushableTask(
			any(),
			any(PushTask.class)
		);

		// We can then verify that register task was called exactly once.
		verify(injectedReactiveDutyCycleTaskRunner, times(1)).registerTask(
			any(Long.class),
			eq(pushTaskObj.getId().toString()), // Ensure the correct pushable ID is passed
			eq("PushableScheduledTask"),
			any(Map.class)
		);
	}
}
