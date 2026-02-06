package com.k_int.pushKb.vault;

import com.k_int.pushKb.Application;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

/*
	What tests are going to be needed for the vault provider service?
	Currently, the existing methods are read/Creat secret and the health check
	We should ensure that when a folio destination is sent the password if present is sent to vault if insecure mode is false/ database is true
	Additionally when a folio destination is fetched the password should be grabbed from the database/vault
	Allow for handling if password isn't present
	Check interaction with incorrect login credentials
 */
@Slf4j
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
public class HashicorpVaultProviderTest {

	@Inject
	ObjectMapper objectmapper;

	@Value("${vault.hashicorp.url:}")
	String baseUrl;
	@Value("${vault.hashicorp.authtype:}")
	String authType;
	@Value("${vault.hashicorp.secret-engine-path:}")
	String secretEnginePath;
	@Value("${vault.hashicorp.token:}")
	String token;

private	HashicorpVaultProvider hashicorpVaultProvider;

	@BeforeEach
	void setup()  {
		hashicorpVaultProvider = new HashicorpVaultProvider(objectmapper,
			baseUrl,
			authType,
			secretEnginePath,
			token,
			"",
			"",
			"",
			"",
			""
		);
	}

	@Test
	void shouldCreateAndReadSecret(){

		String path = "secret-path";
		Map<String, Object> secretMap = Map.of("secret-key", "secret-value");

		Boolean vaultHealth = hashicorpVaultProvider.getVaultHealth();

		hashicorpVaultProvider.createSecret(path, secretMap);

		VaultSecret secret = hashicorpVaultProvider.readSecret("secret-path");

		Assertions.assertNotNull(secret);
		Assertions.assertEquals(true, vaultHealth);
		Assertions.assertEquals("secret-value", secret.data().get("secret-key"));
	}
}
