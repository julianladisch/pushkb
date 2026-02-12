package com.k_int.pushKb.vault;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

// All the configuration needed for vault to function
@ConfigurationProperties("vault")
public interface VaultConfig {
	boolean getInsecure();

	@NonNull
	HashicorpConfig getHashicorpConfig();

	@ConfigurationProperties("hashicorp")
	public interface HashicorpConfig {
		String getUrl();
		String getAuthtype();
		String getSecretEnginePath();

		Optional<String> getToken();
		Optional<String> getUsername();
		Optional<String> getPassword();


		@NonNull
		KubernetesConfig getKubernetesConfig();

		@ConfigurationProperties("kubernetes")
		interface KubernetesConfig {
			Optional<String> getRole();
			Optional<String> getMountPath();
			Optional<String> getServiceAccountTokenPath();
		}
	}
}
