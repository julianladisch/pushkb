package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.vault.VaultSecret;
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
class FolioTenantApiTest extends ServiceIntegrationTest {

	// Helper to keep tests clean
	private HttpResponse<FolioTenant> createInitialTenant(String name, String pass) {
		FolioTenant t = FolioTenant.builder()
			.tenant(name + "Tenant")
			.baseUrl("http://" + name + ".org")
			.name(name)
			.loginUser("made-up-user")
			.loginPassword(pass)
			.authType(FolioAuthType.NONE)
			.build();

		HttpResponse<FolioTenant> response = httpClient.toBlocking().exchange(
			HttpRequest.POST("/destinations/foliodestination/tenant", t),
			FolioTenant.class
		);

		// Assertions on the API Response
		assertEquals(HttpStatus.CREATED, response.getStatus());
		FolioTenant saved = response.body();

		//log.debug("SAVED: {}", saved);
		assertNotNull(saved);
		assertNotNull(saved.getId());
		assertNull(saved.getLoginPassword(), "API should return a sanitized object (no password)");

		// Verify side-effect in the Vault container
		// This proves the service actually pushed the password to the container
		assertVaultSecret(saved.getKey(), "password", pass);

		return response;
	}

	@Test
	void testTenantCreationFlow() {
		// This has assertions built in
		createInitialTenant("create-test", "secret123");
	}

	@Test
	void testTenantPasswordUpdate() {
		HttpResponse<FolioTenant> response = createInitialTenant("update-test", "secret-1");
		FolioTenant tenant = response.body();

		FolioTenant updateRequest = tenant.toBuilder()
			.loginPassword("new-secret-2")
			.build();

		HttpResponse<FolioTenant> putResponse = httpClient.toBlocking().exchange(
			HttpRequest.PUT("/destinations/foliodestination/tenant/" + tenant.getId(), updateRequest),
			FolioTenant.class
		);

		assertEquals(HttpStatus.OK, putResponse.getStatus());

		// 3. Verify Vault has the NEW password
		assertVaultSecret(tenant.getKey(), "password", "new-secret-2");
	}

	@Test
	void testTenantDeletionRemovesVaultSecret() {
		HttpResponse<FolioTenant> response = createInitialTenant("delete-test", "to-be-deleted");
		FolioTenant tenant = response.body();

		HttpResponse<Void> deleteResponse = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/destinations/foliodestination/tenant/" + tenant.getId()),
			Void.class
		);
		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
		assertEquals(Optional.empty(), deleteResponse.getBody());

		VaultSecret secret = vaultProvider.readSecret(tenant.getKey());
		assertTrue(secret.data().isEmpty(), "Vault secret should be removed after tenant deletion");
	}

	@Test
	void testCreateTenantWithMissingRequiredFieldsFails() {
		// Missing baseUrl and tenant name
		FolioTenant invalidTenant = FolioTenant.builder()
			.authType(FolioAuthType.OKAPI)
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.POST("/destinations/foliodestination/tenant", invalidTenant),
				Argument.of(FolioTenant.class),
				Argument.of(PushkbAPIError.class)
			);
			fail("Should have failed validation");
		} catch (HttpClientResponseException e) {
			assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());

			Optional<PushkbAPIError> errorBody = e.getResponse().getBody(PushkbAPIError.class);
			assertTrue(errorBody.isPresent());
			PushkbAPIError error = errorBody.get();

			assertFalse(error.getErrors().isEmpty());
		}
	}

	@Test
	void testDeleteTenantCleansUpVaultEvenIfAlreadyEmpty() {
		// Create a tenant with NONE auth (no vault password)
		FolioTenant t = FolioTenant.builder()
			.tenant("cleanupTenant")
			.baseUrl("http://cleanup.org")
			.name("Cleanup Test")
			.authType(FolioAuthType.NONE)
			.build();

		FolioTenant saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/destinations/foliodestination/tenant", t), FolioTenant.class);

		// Deleting should still succeed (204) even if there was no password to remove
		HttpResponse<Void> response = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/destinations/foliodestination/tenant/" + saved.getId())
		);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
	}

}
