package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	private HttpResponse<GokbSource> ensureTestGokbSource(GokbSource source) {
		try {
			// Try to create
			return httpClient.toBlocking().exchange(
				HttpRequest.POST("/sources/gokbsource", source),
				GokbSource.class
			);
		} catch (io.micronaut.http.client.exceptions.HttpClientResponseException e) {
			// If it already exists (409), just fetch the existing one
			if (e.getStatus() == HttpStatus.CONFLICT) {
				UUID id = GokbSource.generateUUIDFromSource(source);
				return httpClient.toBlocking().exchange(
					HttpRequest.GET("/sources/gokbsource/" + id),
					GokbSource.class
				);
			}
			throw e; // Rethrow if it's a real error (400, 500, etc.)
		}
	}

	@Test
	void testGokbSourceLifecycle() {
		GokbSource source = setupTestGokbSource();

		// This triggers SourceService::ensureSource -> registerIngestTask
		log.info("Testing POST /sources/gokbsource");

		// This should be the first one, so we know it's created not fetched... bit flaky
		HttpResponse<GokbSource> postResponse = ensureTestGokbSource(source);

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

		HttpResponse<GokbSource> ensureResponse = ensureTestGokbSource(source);
		GokbSource saved = ensureResponse.body();

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
		HttpResponse<GokbSource> ensureResponse = ensureTestGokbSource(source);
		GokbSource saved = ensureResponse.body();
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
		HttpResponse<GokbSource> ensureResponse = ensureTestGokbSource(source);
		GokbSource saved = ensureResponse.body();

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

	@Test
	void testCreateDuplicateGokbSourceFails() {
		GokbSource source = setupTestGokbSource();
		// Ensure it's here in case we only run this suite
		HttpResponse<GokbSource> ensureResponse = ensureTestGokbSource(source);


		// Second POST - Same data, must fail with 409
		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/sources/gokbsource", source),
				Argument.of(GokbSource.class),
				Argument.of(PushkbAPIError.class) // Capture the error body
			);
			fail("Should have thrown 409 Conflict");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.CONFLICT, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());

			// HTTPClient level PushKBAPIError
			assertEquals("Conflict", errorBody.get().getMessage());
			assertEquals(HttpMethod.POST, errorBody.get().getMethod());
			assertEquals("/sources/gokbsource", errorBody.get().getUri().getPath());

			// Errors section
			List<PushkbAPIError> errors = errorBody.get().getErrors();
			assertEquals(1, errors.size());
			PushkbAPIError innerError = errors.get(0);
			assertTrue(innerError.getMessage().contains("already exists. Use PUT to update."));
		}
	}

	@Test
	void testResetPointerForNonExistentSourceFails() {
		UUID randomId = UUID.randomUUID();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/sources/gokbsource/" + randomId + "/resetPointer", null),
				Argument.of(GokbSource.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 404 Not Found");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());

			// HTTPClient level PushKBAPIError
			assertEquals("Not Found", errorBody.get().getMessage());

			// Errors section
			List<PushkbAPIError> errors = errorBody.get().getErrors();
			assertEquals(1, errors.size());
			PushkbAPIError innerError = errors.get(0);
			assertEquals("GokbSource not found: " + randomId, innerError.getMessage());
		}
	}

	@Test
	void testDeleteNonExistentSourceFails() {
		UUID randomId = UUID.randomUUID();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.DELETE("/sources/gokbsource/" + randomId),
				Argument.of(Void.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 404 Not Found");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());

			// HTTPClient level PushKBAPIError
			assertEquals("Not Found", errorBody.get().getMessage());

			// Errors section
			List<PushkbAPIError> errors = errorBody.get().getErrors();
			assertEquals(1, errors.size());
			PushkbAPIError innerError = errors.get(0);
			assertEquals("GokbSource not found: " + randomId, innerError.getMessage());
		}
	}

	@Test
	void testCreateInvalidGokbSourceFails() {
		// GokbSource requires a name; sending one with null name should trigger @Valid
		GokbSource invalidSource = GokbSource.builder()
			.name(null)
			.gokb(Gokb.builder().name("Test").baseUrl("http://test.com").build())
			.gokbSourceType(GokbSourceType.PACKAGE)
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/sources/gokbsource", invalidSource),
				Argument.of(GokbSource.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have failed validation");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			// Verify that the sub-errors list is populated by the provider
			assertNotNull(error.getErrors());
			assertFalse(error.getErrors().isEmpty());

			// Check that the specific path "name" is flagged
			boolean hasNameError = error.getErrors().stream()
				.anyMatch(err -> "src.name: must not be null".equals(err.getMessage()));

			assertTrue(hasNameError, "Validation error should identify the 'name' field");
		}
	}

	@Test
	void testPutWithMismatchedDeterministicIdFails() {
		GokbSource source = setupTestGokbSource();
		GokbSource saved = ensureTestGokbSource(source).body();

		// Create an update that would result in a DIFFERENT UUIDv5
		GokbSource mismatchUpdate = saved.toBuilder()
			.gokbSourceType(GokbSourceType.TIPP) // This changes the generated UUID
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/sources/gokbsource/" + saved.getId(), mismatchUpdate),
				Argument.of(GokbSource.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 400 due to ID mismatch");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());

			// HTTPClient level PushKBAPIError
			assertEquals("Bad Request", errorBody.get().getMessage());

			// Errors section
			List<PushkbAPIError> errors = errorBody.get().getErrors();
			assertEquals(1, errors.size());
			PushkbAPIError innerError = errors.get(0);
			assertEquals("This update operation would result in a change of the deterministic ID. Immutable fields cannot be modified.", innerError.getMessage());
		}
	}
}
