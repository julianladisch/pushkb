package com.k_int.pushKb.api.publicApi;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.pushKb.transform.model.ProteusTransform;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PublicTemporaryPushTaskApiTest extends ServiceIntegrationTest {

	private PushTask parentPushTask;

	@BeforeEach
	void setup() {
		// Create a persistent PushTask to act as the parent for temporary tasks
		PushTask pt = PushTask.builder()
			.sourceId(UUID.randomUUID())
			.sourceType(GokbSource.class)
			.destinationId(UUID.randomUUID())
			.destinationType(FolioDestination.class)
			.transformId(UUID.randomUUID())
			.transformType(ProteusTransform.class)
			.build();

		parentPushTask = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/pushtasks", pt),
			PushTask.class
		);
	}

	@Test
	void testCreateTemporaryPushTaskSuccess() {
		// 1. Create the Temporary Task via the PUBLIC endpoint
		// We send a Map to match the expected parameters: UUID pushTaskId, String filterContext
		Map<String, Object> body = Map.of(
			"pushTaskId", parentPushTask.getId(),
			"filterContext", "public-test-context"
		);

		HttpResponse<TemporaryPushTask> response = httpClient.toBlocking().exchange(
			HttpRequest.POST("/public/temporarypushtask", body),
			TemporaryPushTask.class
		);

		assertEquals(HttpStatus.OK, response.getStatus());
		TemporaryPushTask tpt = response.body();
		assertNotNull(tpt);
		assertNotNull(tpt.getId());
		assertEquals("public-test-context", tpt.getFilterContext());
		assertEquals(parentPushTask.getId(), tpt.getPushTask().getId());
	}

	@Test
	void testCreateTemporaryPushTaskNotFound() {
		UUID fakeId = UUID.randomUUID();
		Map<String, Object> body = Map.of("pushTaskId", fakeId);

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/public/temporarypushtask", body),
				Argument.of(TemporaryPushTask.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have returned 404");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();


			List<PushkbAPIError> internalErrors = error.getErrors();
			assertEquals(1, internalErrors.size());


			assertTrue(internalErrors.get(0).getMessage().contains("No PushTask found with id"));
		}
	}

	@Test
	void testCreateDuplicateTemporaryPushTaskConflict() {
		Map<String, Object> body = Map.of(
			"pushTaskId", parentPushTask.getId(),
			"filterContext", "conflict-test"
		);

		// First one succeeds
		httpClient.toBlocking().exchange(HttpRequest.POST("/public/temporarypushtask", body));

		// Second one with same parent and context should return 409 Conflict
		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/public/temporarypushtask", body),
				Argument.of(TemporaryPushTask.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have returned 409");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.CONFLICT, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();


			List<PushkbAPIError> internalErrors = error.getErrors();
			assertEquals(1, internalErrors.size());


			assertTrue(internalErrors.get(0).getMessage().contains("one already exists"));
		}
	}
}
