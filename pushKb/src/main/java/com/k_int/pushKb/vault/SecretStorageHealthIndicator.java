package com.k_int.pushKb.vault;

import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Singleton
@Slf4j
public class SecretStorageHealthIndicator implements HealthIndicator {
	VaultProvider vaultProvider;
	// Inject the same URL used for your Vault configuration
	public SecretStorageHealthIndicator(VaultProvider vaultProvider) {
		this.vaultProvider = vaultProvider;
	}

	@Override
	public Publisher<HealthResult> getResult() {
		return Mono.fromCallable(() -> {
			boolean vaultHealthy = false;
			String vaultError = null;

			try {
				vaultHealthy = vaultProvider.getVaultHealth();
			} catch (Exception e) {
				vaultError = e.getMessage();
			}

			boolean insecureFallbackAllowed = vaultProvider.getVaultConfig().getInsecure();

			if (vaultHealthy && !insecureFallbackAllowed) {
				return HealthResult.builder("secrets", HealthStatus.UP)
					.details(Map.of(
						"mode", "SECURE"
					))
					.build();
			} else if (vaultHealthy) {
				return HealthResult.builder("secrets", HealthStatus.UP)
					.details(Map.of(
						"mode", "SECURE",
						"warning", "Vault is configured, but insecure mode is active, and so a vault failure will result in insecure secret storage"
					))
					.build();
			} else if (insecureFallbackAllowed) {
				return HealthResult.builder("secrets", HealthStatus.UP)
					.details(Map.of(
						"mode", "INSECURE",
						"warning", "Primary Vault unreachable. Falling back to insecure local storage.",
						"vaultError", vaultError != null ? vaultError : "Vault Sealed/Uninitialized"
					))
					.build();
			}

			return HealthResult.builder("secrets", HealthStatus.DOWN)
				.details(Map.of(
					"error", vaultError != null ? vaultError : "Vault unavailable and insecure mode disabled"
				))
				.build();
		});
	}
}
