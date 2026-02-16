package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioDestinationType;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FolioDestinationApiTest extends ServiceIntegrationTest {

	private FolioDestination setupTestDestination(String name) {
		FolioTenant tenant = FolioTenant.builder()
			.tenant(name + "_tenant")
			.baseUrl("http://" + name + ".folio.org")
			.name(name + " Tenant")
			.loginUser("admin")
			.loginPassword("secret")
			.authType(FolioAuthType.OKAPI)
			.build();

		return FolioDestination.builder()
			.name(name + " Destination")
			.destinationType(FolioDestinationType.PCI) // Assuming PCI exists in your enum
			.folioTenant(tenant)
			.build();
	}

	@Test
	void testFolioDestinationLifecycle() {
		FolioDestination dest = setupTestDestination("lifecycle-test");

		// 1. POST - Create Destination (Side effect: ensures FolioTenant is created)
		log.info("Testing POST /destinations/foliodestination");
		HttpResponse<FolioDestination> postResponse = httpClient.toBlocking().exchange(
			HttpRequest.POST("/destinations/foliodestination", dest),
			FolioDestination.class
		);

		assertEquals(HttpStatus.CREATED, postResponse.getStatus());
		FolioDestination saved = postResponse.body();
		assertNotNull(saved);
		assertNotNull(saved.getId(), "Destination ID should be generated");
		assertNotNull(saved.getFolioTenant().getId(), "Nested Tenant ID should have been generated and saved");

		// 2. GET - Verify Persistence
		FolioDestination fetched = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/destinations/foliodestination/" + saved.getId()),
			FolioDestination.class
		);
		assertEquals(saved.getName(), fetched.getName());
		assertEquals(saved.getFolioTenant().getTenant(), fetched.getFolioTenant().getTenant());

		// 3. DELETE - Cleanup
		log.info("Testing DELETE /destinations/foliodestination/{}", saved.getId());
		HttpResponse<Void> deleteResponse = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/destinations/foliodestination/" + saved.getId()),
			Void.class
		);

		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
		assertNull(deleteResponse.body());
	}

	@Test
	void testUpdateDoesNotDuplicateTenant() {
		// 1. Create initial destination
		FolioDestination initial = setupTestDestination("duplication-test");
		FolioDestination saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/destinations/foliodestination", initial),
			FolioDestination.class
		);
		UUID originalTenantId = saved.getFolioTenant().getId();

		// 2. Perform a PUT update on the Destination
		FolioDestination updateRequest = saved.toBuilder()
			.name("Renamed Destination")
			.build();

		HttpResponse<FolioDestination> putResponse = httpClient.toBlocking().exchange(
			HttpRequest.PUT("/destinations/foliodestination/" + saved.getId(), updateRequest),
			FolioDestination.class
		);

		assertEquals(HttpStatus.OK, putResponse.getStatus());

		// 3. Verify the Tenant ID remained the same (didn't recreate or mismatch)
		assertEquals(originalTenantId, putResponse.body().getFolioTenant().getId(),
			"The underlying Tenant should remain the same during a Destination update");
	}

	@Test
	void testMismatchedIdFails() {
		FolioDestination dest = setupTestDestination("mismatch-test");
		FolioDestination saved = httpClient.toBlocking().retrieve(
			HttpRequest.POST("/destinations/foliodestination", dest),
			FolioDestination.class
		);

		// Try to PUT to the correct ID, but send a body that would generate a DIFFERENT deterministic ID
		// Changing the DestinationType will trigger your CrudControllerImpl.java:67 check
		FolioDestination badBody = saved.toBuilder()
			.destinationType(FolioDestinationType.PACKAGE)
			.build();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/destinations/foliodestination/" + saved.getId(), badBody),
				FolioDestination.class
			);
			fail("Should have thrown a 400/500 due to ID mismatch");
		} catch (io.micronaut.http.client.exceptions.HttpClientResponseException e) {
			// Per our previous discussion, this is currently a 500 in your CrudControllerImpl
			assertTrue(e.getStatus().getCode() >= 400);
			log.info("Caught expected error on ID mismatch: {}", e.getMessage());
		}
	}
}
