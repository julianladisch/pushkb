package com.k_int.pushKb.services;

import com.k_int.pushKb.Application;
import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.interactions.folio.services.FolioTenantDatabaseService;

import com.k_int.pushKb.vault.VaultProvider;
import com.k_int.pushKb.vault.VaultSecret;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@Slf4j
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
public class FolioTenantDatabaseServiceTest {

	@Inject
	VaultProvider vaultProvider;

	@Inject
	FolioTenantDatabaseService folioTenantDatabaseService;

	final UUID tenantId = UUID.randomUUID();

	final String password = "TEST_PASSWORD";

	FolioTenant tenant = FolioTenant.builder()
		.id(tenantId)
		.authType(FolioAuthType.NONE)
		.baseUrl("")
		.tenant("")
		.name("")
		.loginUser("")
		.loginPassword(password)
		.build();

	@BeforeEach
	void setup() {
		folioTenantDatabaseService = Mockito.spy(folioTenantDatabaseService); // This is needed to run mockito.verify() on registerPushableTask() below.
	}

	@Test
	void shouldSaveLoginPasswordInVault() {
		Mono<FolioTenant> testPipeline = Mono.from(folioTenantDatabaseService.ensureFolioTenant(tenant)).cache();

		// This runs the reactive code above, then we can assert against the resulting tuple.
		StepVerifier.create(testPipeline)
			.assertNext(savedTenant -> {
				VaultSecret tenantPassword = vaultProvider.readSecret(savedTenant.getKey());

				Assertions.assertNotNull(savedTenant);
				Assertions.assertEquals(password, tenantPassword.data().get("password"));
			})
			.verifyComplete();
	}
}
