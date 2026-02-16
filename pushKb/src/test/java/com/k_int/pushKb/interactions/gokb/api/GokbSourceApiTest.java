package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class GokbSourceApiTest extends ServiceIntegrationTest {

	@Inject
	ReactiveDutyCycleTaskRepository dutyCycleTaskRepository;


	private GokbSource setupTestGokbSource() {
		return GokbSource.builder()
			.name("Test GOKB Source")
			.gokb(
				Gokb.builder()
					.baseUrl("https://gokb.org/gokb/api")
					.name("Test_GOKB")
					.build()
			)
			.gokbSourceType(GokbSourceType.PACKAGE)
			.build();
	}

	@Test
	void testGokbSourceLifecycle() {
		GokbSource source = setupTestGokbSource();

		// This triggers SourceService::ensureSource -> registerIngestTask
		log.info("Testing POST /sources/gokbsource");
		HttpResponse<GokbSource> postResponse = httpClient.toBlocking().exchange(
			HttpRequest.POST("/sources/gokbsource", source),
			GokbSource.class
		);

		assertEquals(HttpStatus.CREATED, postResponse.getStatus());
		GokbSource saved = postResponse.body();
		assertNotNull(saved);
		assertNotNull(saved.getId(), "Source ID should be generated");

		// VERIFY SIDE EFFECT: DutyCycleTask exists in the DB
		// Since countByReference doesn't exist, we use existsByReference
		Boolean taskExists = Mono.from(dutyCycleTaskRepository.existsByReference(saved.getId().toString()))
			.block();

		assertNotNull(taskExists);
		assertTrue(taskExists, "An IngestScheduledTask should have been registered for this source ID");

		GokbSource fetched = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/sources/gokbsource/" + saved.getId()),
			GokbSource.class
		);
		assertEquals(saved.getName(), fetched.getName());
		assertNotNull(fetched.getGokb());
		assertEquals("https://gokb.org/gokb/api", fetched.getGokb().getBaseUrl());

		// This triggers SourceService::delete -> reactiveDutyCycleTaskRunner.removeTask
		log.info("Testing DELETE /sources/gokbsource/{}", saved.getId());
		HttpResponse<Void> deleteResponse = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/sources/gokbsource/" + saved.getId()),
			Void.class
		);

		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
		// body() on a Long return usually yields an Optional<Long>
		assertNull(deleteResponse.body(), "delete body should be empty");

		// VERIFY SIDE EFFECT: DutyCycleTask is gone
		Boolean taskStillExists = Mono.from(dutyCycleTaskRepository.existsByReference(saved.getId().toString()))
			.block();

		assertNotNull(taskStillExists);
		assertFalse(taskStillExists, "The DutyCycleTask should have been removed from the DB upon source deletion");
	}

	@Test
	void testResetPointer() {
		GokbSource source = setupTestGokbSource();

		GokbSource saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/sources/gokbsource", source),
			GokbSource.class
		);

		// Call the resetPointer endpoint
		HttpResponse<GokbSource> resetResponse = httpClient.toBlocking().exchange(
			HttpRequest.PUT("/sources/gokbsource/" + saved.getId() + "/resetPointer", null),
			GokbSource.class
		);

		assertEquals(HttpStatus.OK, resetResponse.getStatus());
		assertNull(resetResponse.body().getPointer(), "Pointer should be null after reset");
	}

	@Test
	void testUpdateDoesNotChangeNestedGokb() {
		// 1. Setup and save initial
		GokbSource source = setupTestGokbSource();
		GokbSource saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/sources/gokbsource", source),
			GokbSource.class
		);
		String originalGokbName = saved.getGokb().getName();

		// 2. Attempt to change the nested Gokb baseUrl via a PUT on GokbSource
		GokbSource updateRequest = saved.toBuilder()
			.name("Updated Name")
			.gokb(saved.getGokb().toBuilder().name("This change will not make it").build())
			.build();

		httpClient.toBlocking().exchange(
			HttpRequest.PUT("/sources/gokbsource/" + saved.getId(), updateRequest),
			GokbSource.class
		);

		// 3. Fetch fresh and verify: Name changed, but Gokb BaseUrl DID NOT
		GokbSource fresh = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/sources/gokbsource/" + saved.getId()),
			GokbSource.class
		);

		assertEquals("Updated Name", fresh.getName());
		assertEquals(originalGokbName, fresh.getGokb().getName(),
			"The nested Gokb object should not be updatable via the GokbSource endpoint");
	}

	@Test
	void testChangingSourceTypeFails() {
		GokbSource source = setupTestGokbSource(); // This is GokbSourceType.PACKAGE
		GokbSource saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/sources/gokbsource", source),
			GokbSource.class
		);

		GokbSource updateRequest = saved.toBuilder()
			.gokbSourceType(GokbSourceType.TIPP)
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/sources/gokbsource/" + saved.getId(), updateRequest),
				GokbSource.class
			);
		} catch (io.micronaut.http.client.exceptions.HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus(), "Expected 400 when changing immutable source type");
		}
	}
}
