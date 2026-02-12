package com.k_int.pushKb.health;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

// Not using ServiceIntegrationTest as it is INTENTIONALLY failing
@Slf4j
@MicronautTest(
	environments = "test",
	transactional = false,
	rollback = false
)
@Property(name = "vault.hashicorp.url", value = "http://made-up-url:1234")
class WrongVaultAddressHealthApiTest {

	@Inject
	@Client("/")
	protected HttpClient httpClient;

	@Inject
	protected JsonMapper jsonMapper;

	@Test
	void testHealthWhenVaultIsDown() throws IOException {
		// Micronaut returns 503 for any DOWN status
		HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> httpClient.toBlocking().exchange(HttpRequest.GET("/health"), JsonNode.class));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatus());

		JsonNode body = e.getResponse().getBody(JsonNode.class).orElseThrow();
		log.info("Vault DOWN Response: {}", jsonMapper.writeValueAsString(body));

		// Verify aggregate and specific status
		assertEquals("DOWN", body.get("status").getStringValue());

		JsonNode secretDetails = body.get("details").get("secrets");
		assertEquals("DOWN", secretDetails.get("status").getStringValue());

		// Check that our provider's error message made it through
		String error = secretDetails.get("details").get("error").getStringValue();
		assertTrue(error.contains("Vault IO Failure"), "Expected IO Failure message but got: " + error);
	}
}
