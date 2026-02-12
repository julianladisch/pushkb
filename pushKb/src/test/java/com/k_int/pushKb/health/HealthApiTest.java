package com.k_int.pushKb.health;

import com.k_int.pushKb.test.ServiceIntegrationTest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.json.tree.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class HealthApiTest extends ServiceIntegrationTest {
	@Test
	void testHealthEndpointReturnsStatus() throws IOException {
		// Micronaut default health endpoint is /health
		HttpResponse<JsonNode> response = httpClient.toBlocking().exchange(
			HttpRequest.GET("/health"),
			JsonNode.class
		);

		assertEquals(HttpStatus.OK, response.getStatus());
		JsonNode body = response.body();
		assertNotNull(body);

		// Check top-level status
		assertEquals("UP", body.get("status").getStringValue());

		log.info("Health Status: {}", jsonMapper.writeValueAsString(body));
	}

	@Test
	void testVaultHealthDetails() throws IOException {
		HttpResponse<JsonNode> response = httpClient.toBlocking().exchange(
			HttpRequest.GET("/health"),
			JsonNode.class
		);

		log.info("Health Status: {}", jsonMapper.writeValueAsString(response.body()));

		JsonNode details = response.body().get("details");
		JsonNode secrets = details.get("secrets");

		assertNotNull(details, "Health details should be visible");
		assertNotNull(secrets, "Vault details should be visible");



		String vaultStatus = secrets.get("status").getStringValue();
		JsonNode vaultDetails = secrets.get("details");
		assertEquals("UP", vaultStatus);
		assertEquals("SECURE", vaultDetails.get("mode").getStringValue());

		// If you have a custom VaultHealthIndicator, it would show up here
		// If not, we are verifying the vaultProvider's state via the Startup Listener logs
		assertTrue(vaultProvider.getVaultHealth(), "Vault should be healthy in our test container environment");
	}
}
