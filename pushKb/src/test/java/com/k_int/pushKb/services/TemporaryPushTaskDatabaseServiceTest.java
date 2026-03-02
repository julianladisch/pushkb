package com.k_int.pushKb.services;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.pushKb.transform.model.ProteusTransform;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

class TemporaryPushTaskDatabaseServiceTest extends ServiceIntegrationTest {

	@Inject
	TemporaryPushTaskDatabaseService temporaryPushTaskDatabaseService;

	@Inject
	PushTaskDatabaseService pushTaskDatabaseService;

	private TemporaryPushTask createTPT(PushTask parent, String filterContext) {
		return TemporaryPushTask.builder()
			.pushTask(parent)
			.filterContext(filterContext)
			.build();
	}

	@Test
	void testTemporaryPushTaskLifecycle() {
		// Setup: We need a real PushTask for the foreign key reference
		// In a real scenario, this would be created via PushTaskApi
		PushTask parent = PushTask.builder()
			.sourceId(UUID.randomUUID())
			.sourceType(GokbSource.class)
			.destinationId(UUID.randomUUID())
			.destinationType(FolioDestination.class)
			.transformId(UUID.randomUUID())
			.transformType(ProteusTransform.class)
			.build();


		Mono<TemporaryPushTask> lifecyclePipeline = Mono.from(pushTaskDatabaseService.ensurePushable(parent))
			.flatMap(savedParent -> {
				TemporaryPushTask tpt = createTPT(savedParent, "test-context");

				return Mono.from(temporaryPushTaskDatabaseService.ensurePushable(tpt))
					.flatMap(savedTpt -> {
						Assertions.assertNotNull(savedTpt.getId());

						// 2. Ensure Pushable (Get existing) - exercises the 'doesItExist' branch
						return Mono.from(temporaryPushTaskDatabaseService.ensurePushable(savedTpt))
							.map(existing -> {
								Assertions.assertEquals(savedTpt.getId(), existing.getId());
								return existing;
							});
					});
			});

		StepVerifier.create(lifecyclePipeline)
			.assertNext(tpt -> Assertions.assertNotNull(tpt.getId()))
			.verifyComplete();
	}

	@Test
	void testGetFeedAndComplete() {
		PushTask parent = PushTask.builder()
			.sourceId(UUID.randomUUID())
			.sourceType(GokbSource.class)
			.destinationId(UUID.randomUUID())
			.destinationType(FolioDestination.class)
			.transformId(UUID.randomUUID())
			.transformType(ProteusTransform.class)
			.build();

		Mono<Boolean> completionPipeline = Mono.from(pushTaskDatabaseService.ensurePushable(parent))
			.flatMap(savedParent -> Mono.from(temporaryPushTaskDatabaseService.ensurePushable(createTPT(savedParent, "feed-test"))))
			.flatMap(savedTpt -> Flux.from(temporaryPushTaskDatabaseService.getFeed())
				.collectList()
				.flatMap(list -> {
					Assertions.assertFalse(list.isEmpty());
					return Mono.from(temporaryPushTaskDatabaseService.complete(savedTpt));
				}));

		StepVerifier.create(completionPipeline)
			.expectNext(true)
			.verifyComplete();
	}
}
