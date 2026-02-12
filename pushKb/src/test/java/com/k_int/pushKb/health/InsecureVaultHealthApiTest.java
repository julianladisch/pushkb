package com.k_int.pushKb.health;

import com.k_int.pushKb.test.ServiceIntegrationTest;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(
	environments = "test",
	transactional = false,
	rollback = false
)
@Property(name = "vault.insecure", value = "true")
class InsecureVaultHealthApiTest extends ServiceIntegrationTest {
	@Test
	void testHealthWithInsecureFlagActive() {
		HttpResponse<JsonNode> response = httpClient.toBlocking().exchange(HttpRequest.GET("/health"), JsonNode.class);
		JsonNode secrets = response.body().get("details").get("secrets");

		assertEquals("UP", secrets.get("status").getStringValue());
		assertEquals("SECURE", secrets.get("details").get("mode").getStringValue());
		// Verify the specific warning for the "Hybrid" state
		assertTrue(secrets.get("details").get("warning").getStringValue().contains("insecure mode is active"));
	}
}
