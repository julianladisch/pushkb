package com.k_int.pushKb.health;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(
	environments = "test",
	transactional = false,
	rollback = false
)
@Property(name = "vault.insecure", value = "true")
@Property(name = "vault.hashicorp.url", value = "http://localhost:1234") // Bad URL
class VaultFallbackHealthApiTest {
	@Inject
	@Client("/")
	HttpClient httpClient;

	@Test
	void testHealthFallbackToInsecure() {
		HttpResponse<JsonNode> response = httpClient.toBlocking().exchange(HttpRequest.GET("/health"), JsonNode.class);
		JsonNode secrets = response.body().get("details").get("secrets");

		assertEquals("UP", secrets.get("status").getStringValue());
		assertEquals("INSECURE", secrets.get("details").get("mode").getStringValue());
		assertTrue(secrets.get("details").get("warning").getStringValue().contains("Falling back"));
	}
}
