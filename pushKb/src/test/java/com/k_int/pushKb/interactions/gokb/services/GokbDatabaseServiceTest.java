package com.k_int.pushKb.interactions.gokb.services;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

class GokbDatabaseServiceTest extends ServiceIntegrationTest {

	@Inject
	GokbDatabaseService gokbDatabaseService;

	@Test
	void testEnsureGokbLogic() {
		Gokb gokb = Gokb.builder()
			.name("Test GOKB Instance")
			.baseUrl("https://gokb.example.org/api")
			.build();

		UUID expectedId = Gokb.generateUUIDFromGoKB(gokb);

		Mono<Gokb> firstEnsure = Mono.from(gokbDatabaseService.ensureGokb(gokb));

		StepVerifier.create(firstEnsure)
			.assertNext(saved -> {
				Assertions.assertEquals(expectedId, saved.getId());
				Assertions.assertEquals("Test GOKB Instance", saved.getName());
			})
			.verifyComplete();

		Gokb modifiedGokb = Gokb.builder()
			.name("Different Name Same URL")
			.baseUrl("https://gokb.example.org/api")
			.build();

		Mono<Gokb> secondEnsure = Mono.from(gokbDatabaseService.ensureGokb(modifiedGokb));

		StepVerifier.create(secondEnsure)
			.assertNext(existing -> {
				Assertions.assertEquals(expectedId, existing.getId());
				// Crucial check: ensureGokb logic in repository favors database state over input state
				Assertions.assertEquals("Test GOKB Instance", existing.getName(),
					"Should have returned the original record from DB");
			})
			.verifyComplete();
	}
}
