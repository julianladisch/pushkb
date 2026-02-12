package com.k_int.pushKb.vault;

import com.k_int.pushKb.Application;

import io.micronaut.context.env.Environment;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/*
	What tests are going to be needed for the vault provider service?
	Currently, the existing methods are read/Creat secret and the health check
	We should ensure that when a folio destination is sent the password if present is sent to vault if insecure mode is false/ database is true
	Additionally when a folio destination is fetched the password should be grabbed from the database/vault
	Allow for handling if password isn't present
	Check interaction with incorrect login credentials
 */
// This doesn't use ServiceIntegrationTest interface because we need to FORCE a
// hashicorp vault to test hashicorp vault
@Slf4j
@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false,transactional = false, rollback = true)
public class HashicorpVaultProviderTest {

	@Inject
	ObjectMapper objectmapper;

	@Inject
	VaultConfig vaultConfig;

	private	HashicorpVaultProvider hashicorpVaultProvider;

	@BeforeEach
	void setup()  {

		// Cheat a VaultConfig
		VaultConfig theConfig = new VaultConfig() {
			@Override
			public boolean getInsecure() {
				return false;
			}

			@Override
			public HashicorpConfig getHashicorpConfig() {

				return new HashicorpConfig() {
					@Override
					public String getUrl() {
						return vaultConfig.getHashicorpConfig().getUrl();
					}

					@Override
					public String getAuthtype() {
						return vaultConfig.getHashicorpConfig().getAuthtype();
					}

					@Override
					public String getSecretEnginePath() {
						return vaultConfig.getHashicorpConfig().getSecretEnginePath();
					}

					@Override
					public Optional<String> getToken() {
						return vaultConfig.getHashicorpConfig().getToken();
					}

					@Override
					public Optional<String> getUsername() {
						return Optional.empty();
					}

					@Override
					public Optional<String> getPassword() {
						return Optional.empty();
					}

					@Override
					public HashicorpConfig.KubernetesConfig getKubernetesConfig() {
						return new HashicorpConfig.KubernetesConfig() {
							@Override
							public Optional<String> getRole() {
								return Optional.empty();
							}

							@Override
							public Optional<String> getMountPath() {
								return Optional.empty();
							}

							@Override
							public Optional<String> getServiceAccountTokenPath() {
								return Optional.empty();
							}
						};
					}
				};
			}
		};

		hashicorpVaultProvider = new HashicorpVaultProvider(
			objectmapper,
			theConfig
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
