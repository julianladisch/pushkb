package com.k_int.pushKb.test;

import com.k_int.pushKb.vault.VaultProvider;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures there is a provided vault and http client for integration test use
 */
@MicronautTest(
	environments = "test",
	transactional = false,
	rollback = false
)
public abstract class ServiceIntegrationTest {
	@Inject
	protected VaultProvider vaultProvider;

	@Inject
	@Client("/")
	protected HttpClient httpClient;

	@BeforeEach
	void setupAndVerifyEnvironment() {
		// 1. Ensure DI worked
		assertNotNull(vaultProvider, "VaultProvider not injected.");

		// 2. Ensure the real Vault Testcontainer is actually ready
		// This stops the test immediately if the container failed to start/unseal
		assertTrue(vaultProvider.getVaultHealth(),
				"Vault Testcontainer is unhealthy. Check Docker or Test Resources logs.");
	}

	/**
	 * Helper to verify a secret actually made it into the container
	 */
	protected void assertVaultSecret(String path, String key, Object expectedValue) {
			var secret = vaultProvider.readSecret(path);
			assertNotNull(secret, "Secret missing at path: " + path);
			assertEquals(expectedValue, secret.data().get(key));
	}
}
