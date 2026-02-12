package com.k_int.pushKb.test;

import com.k_int.pushKb.vault.VaultProvider;
import io.micronaut.json.JsonMapper;
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

	@Inject
	protected JsonMapper jsonMapper;

	@BeforeEach
	void setupAndVerifyEnvironment() {
		assertNotNull(vaultProvider, "VaultProvider not injected.");

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
