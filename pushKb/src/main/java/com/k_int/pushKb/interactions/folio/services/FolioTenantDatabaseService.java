package com.k_int.pushKb.interactions.folio.services;


import java.util.Map;
import java.util.UUID;

import com.k_int.pushKb.crud.CrudDatabaseService;
import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.vault.VaultProvider;
import com.k_int.pushKb.vault.VaultSecret;
import io.micronaut.context.annotation.Value;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.interactions.folio.storage.FolioTenantRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class FolioTenantDatabaseService implements CrudDatabaseService<FolioTenant> {
	private final FolioTenantRepository folioTenantRepository;
	private final VaultProvider vaultProvider;

	boolean isInsecureMode;

	public FolioTenantDatabaseService(
		FolioTenantRepository folioTenantRepository,
		VaultProvider vaultProvider,
		@Value("${vault.insecure:false}") boolean isInsecureMode
	) {
		this.folioTenantRepository = folioTenantRepository;
		this.vaultProvider = vaultProvider;
		this.isInsecureMode = isInsecureMode;
	}

	public FolioTenantRepository getRepository() {
		return folioTenantRepository;
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<FolioTenant> ensureFolioTenant(FolioTenant folioTenant) {
		UUID gen_id = FolioTenant.generateUUIDFromFolioTenant(folioTenant);
		folioTenant.setId(gen_id);

		return Mono.from(folioTenantRepository.existsById(gen_id))
			.flatMap(doesItExist -> Mono.from(doesItExist ?
				folioTenantRepository.findById(gen_id) :
				save(folioTenant)
			));
	}

	private void handleLoginPasswordVaultSecret(String key, Map<String, Object> secret) {
		if (secret == null) {
			log.warn("No secret configured for folioTenant: {}, skipping vault", key);
			return;
		}

		VaultSecret password = vaultProvider.readSecret(key);


		if (password.data().isEmpty()) {
			// Assume no password present in vault so we need to create one
			vaultProvider.createSecret(key, secret);
			// Now that we've created the password secret based on the ID we now need to remove it from the tenant
		} else {
			// We need to update secret in the case where a password already exists
			vaultProvider.updateSecret(key, secret);
		}
	}

	private void validateVaultEnvironment() {
		if (!vaultProvider.getVaultHealth() && !isInsecureMode) {
			throw new IllegalStateException(
				"Cannot handle folio tenants when vault is unavailable and insecure mode is disabled"
			);
		}
	}

	@Override
	@Transactional
	public Publisher<FolioTenant> update(FolioTenant folioTenant) {
		return Mono.defer(() -> {
			validateVaultEnvironment();

			// If Vault is active, handle the secret first, so we can quit out if it fails
			if (vaultProvider.getVaultHealth()) {
				try {
					if (folioTenant.getLoginPassword() != null) {
						handleLoginPasswordVaultSecret(
							folioTenant.getKey(),
							Map.of("password", folioTenant.getLoginPassword())
						);
					}
					// If the code reaches here, Vault succeeded.
					// Return the sanitized version for DB storage.
					return Mono.from(folioTenantRepository.update(FolioTenant.sanitiseFolioTenant(folioTenant)));
				} catch (Exception e) {
					log.error("Vault operation failed; aborting save for {}", folioTenant.getTenant(), e);
					return Mono.error(e);
				}
			}


			return Mono.from(folioTenantRepository.update(folioTenant));
		});
	}

	@Override
	@Transactional
	public Publisher<FolioTenant> save(FolioTenant folioTenant) {
		return Mono.defer(() -> {
			validateVaultEnvironment();

			// If Vault is active, handle the secret first, so we can quit out if it fails
			if (vaultProvider.getVaultHealth()) {
				try {
					if (folioTenant.getLoginPassword() != null) {
						handleLoginPasswordVaultSecret(
							folioTenant.getKey(),
							Map.of("password", folioTenant.getLoginPassword())
						);
					}
					// If the code reaches here, Vault succeeded.
					// Return the sanitized version for DB storage.
					return Mono.from(folioTenantRepository.save(FolioTenant.sanitiseFolioTenant(folioTenant)));
				} catch (Exception e) {
					log.error("Vault operation failed; aborting save for {}", folioTenant.getTenant(), e);
					return Mono.error(e);
				}
			}


			return Mono.from(folioTenantRepository.save(folioTenant));
		});
	}

	@Override
	@Transactional
	public Publisher<Long> delete(FolioTenant folioTenant) {
		return Mono.defer(() -> {
			log.info("FolioTenantDatabaseService::delete called " + folioTenant);
			validateVaultEnvironment();

			if (vaultProvider.getVaultHealth()) {
				try {
					// Try Vault cleanup first
					vaultProvider.deleteSecret(folioTenant.getKey());
				} catch (Exception e) {
					log.error("Failed to delete Vault secret; aborting DB deletion", e);
					return Mono.error(e);
				}
			}

			return Mono.from(folioTenantRepository.delete(folioTenant));
		});
	}
}
