package com.k_int.pushKb.interactions.gokb.services;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GokbSourceDatabaseServiceTest extends ServiceIntegrationTest {

	@Inject
	GokbSourceDatabaseService gokbSourceDatabaseService;

	@Inject
	GokbDatabaseService gokbDatabaseService;

	@Test
	void testEnsureSourceOrchestration() {
		Gokb parentGokb = Gokb.builder()
			.name("Parent GOKB")
			.baseUrl("https://shared.gokb.org")
			.build();

		GokbSource source = GokbSource.builder()
			.name("TIPP Source")
			.gokbSourceType(GokbSourceType.TIPP)
			.gokb(parentGokb)
			.build();

		// Chain the operations reactively
		Mono<GokbSource> testPipeline = Mono.from(gokbSourceDatabaseService.ensureSource(source))
			.flatMap(savedSource ->
				// Verify the GOKB was actually persisted by finding it in the DB
				Mono.from(gokbDatabaseService.findById(savedSource.getGokb().getId()))
					.map(dbGokb -> {
						Assertions.assertNotNull(dbGokb, "Parent GOKB should exist in DB");
						Assertions.assertEquals("Parent GOKB", dbGokb.getName());
						return savedSource;
					})
			);

		StepVerifier.create(testPipeline)
			.assertNext(savedSource -> {
				// Verify the Source was persisted correctly
				Assertions.assertNotNull(savedSource.getId());
				Assertions.assertEquals("TIPP Source", savedSource.getName());
				Assertions.assertEquals(GokbSourceType.TIPP, savedSource.getGokbSourceType());
			})
			.verifyComplete();
	}

	@Test
	void testSaveOrUpdateLogic() {
		Gokb parentGokb = Gokb.builder()
			.name("Shared GOKB")
			.baseUrl("https://update.gokb.org")
			.build();

		// Fully reactive pipeline from GOKB creation through Source update
		Mono<GokbSource> updatePipeline = Mono.from(gokbDatabaseService.ensureGokb(parentGokb))
			.flatMap(savedGokb -> {
				GokbSource source = GokbSource.builder()
					.name("Original Name")
					.gokbSourceType(GokbSourceType.PACKAGE)
					.gokb(savedGokb)
					.build();
				source.setId(GokbSource.generateUUIDFromSource(source));

				return Mono.from(gokbSourceDatabaseService.saveOrUpdate(source));
			})
			.flatMap(savedSource -> {
				Assertions.assertNotNull(savedSource);
				Assertions.assertEquals("Original Name", savedSource.getName());

				// Prepare the update
				savedSource.setName("Updated Name");
				return Mono.from(gokbSourceDatabaseService.saveOrUpdate(savedSource));
			});

		StepVerifier.create(updatePipeline)
			.assertNext(updated -> {
				Assertions.assertEquals("Updated Name", updated.getName());
				Assertions.assertNotNull(updated.getId());
			})
			.verifyComplete();
	}
}
