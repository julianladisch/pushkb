package com.k_int.pushKb.transform.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.pushKb.transform.model.ProteusTransform;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ProteusTransformApiTest extends ServiceIntegrationTest {

	@Test
	void testPostMissingIdReturns400BadRequest() {
		Map<String, String> invalidBody = Map.of("name", "Invalid No-ID Transform");

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/transforms/proteustransform/" + UUID.randomUUID(), invalidBody),
				Argument.of(ProteusTransform.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 400 Bad Request due to missing ID");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			assertNotNull(error.getErrors());
			// Verify the sub-error highlights the ID field requirement
			assertTrue(error.getErrors().stream().anyMatch(err -> err.getMessage().contains("id")));
		}
	}

	@Test
	void testPostWithIdReturns405NotAllowed() {
		// Providing a valid ID satisfies validation but triggers the manual override
		ProteusTransform pt = ProteusTransform.builder()
			.id(UUID.randomUUID())
			.name("Valid ID Transform")
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/transforms/proteustransform/" + pt.getId(), pt),
				Argument.of(ProteusTransform.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 405 Method Not Allowed");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.METHOD_NOT_ALLOWED, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			assertNotNull(error.getTimestamp());
			assertFalse(error.getErrors().isEmpty());

			assertTrue(error.getErrors().get(0).getMessage().contains("POST is not currently supported"));
		}
	}

	@Test
	void testPutWithIdReturns405NotAllowed() {
		UUID id = UUID.randomUUID();
		ProteusTransform pt = ProteusTransform.builder()
			.id(id)
			.name("Update Attempt")
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/transforms/proteustransform/" + id, pt),
				Argument.of(ProteusTransform.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 405");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.METHOD_NOT_ALLOWED, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			assertNotNull(error.getTimestamp());
			assertFalse(error.getErrors().isEmpty());

			assertTrue(error.getErrors().get(0).getMessage().contains("PUT is not currently supported"));
		}
	}

	@Test
	void testDeleteReturns405NotAllowed() {
		try {
			httpClient.toBlocking().exchange(
				HttpRequest.DELETE("/transforms/proteustransform/" + UUID.randomUUID()),
				Argument.of(Void.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 405");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.METHOD_NOT_ALLOWED, e.getStatus());
		}
	}
}
