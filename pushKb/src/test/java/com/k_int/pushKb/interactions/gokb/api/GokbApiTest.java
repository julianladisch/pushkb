package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class GokbApiTest extends ServiceIntegrationTest {

	private Gokb createGokbObject(String name, String url) {
		return Gokb.builder()
			.name(name)
			.baseUrl(url)
			.build();
	}

	@Test
	void testGokbLifecycle() {
		Gokb gokb = createGokbObject("Main GOKb", "https://gokb.org");

		HttpResponse<Gokb> postResponse = httpClient.toBlocking().exchange(
			HttpRequest.POST("/sources/gokbsource/gokb", gokb), Gokb.class);

		assertEquals(HttpStatus.CREATED, postResponse.getStatus());
		Gokb saved = postResponse.body();
		assertNotNull(saved.getId());

		Gokb fetched = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/sources/gokbsource/gokb/" + saved.getId()), Gokb.class);
		assertEquals("https://gokb.org", fetched.getBaseUrl());

		HttpResponse<Void> deleteResponse = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/sources/gokbsource/gokb/" + saved.getId()));
		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
	}

	@Test
	void testGokbIdMismatchOnPut() {
		Gokb gokb = createGokbObject("Mismatch Test", "https://mismatch.org");
		Gokb saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/sources/gokbsource/gokb", gokb), Gokb.class);

		// Attempt to change the baseUrl (which is used for the deterministic ID)
		Gokb invalidUpdate = saved.toBuilder().baseUrl("https://new-url.org").build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/sources/gokbsource/gokb/" + saved.getId(), invalidUpdate),
				Argument.of(Gokb.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have thrown 400 Bad Request");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			// Verify our standard error structure
			assertNotNull(error.getTimestamp());
			assertTrue(error.getErrors().stream()
				.anyMatch(err -> err.getMessage().contains("Immutable fields")));
		}
	}
}
