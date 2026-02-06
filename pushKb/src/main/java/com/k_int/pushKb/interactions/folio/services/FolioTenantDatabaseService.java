package com.k_int.pushKb.interactions.folio.services;


import java.util.Map;
import java.util.UUID;

import com.k_int.pushKb.crud.CrudDatabaseService;
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
  public Publisher<FolioTenant> ensureFolioTenant( FolioTenant folioTenant ) {
		UUID gen_id = FolioTenant.generateUUIDFromFolioTenant(folioTenant);
		folioTenant.setId(gen_id);

		boolean isVaultConfigured = vaultProvider.getVaultHealth();

		if (!isVaultConfigured && !isInsecureMode) {
			return Mono.error(new IllegalStateException(
				"Cannot handle folio tenants when vault is unavailable and insecure mode is disabled"
			));
		}

		FolioTenant tenantToPersist;
		// Insecure mode and vault interactions
		if(isVaultConfigured){
			handleLoginPasswordVaultSecret(
				folioTenant.getKey(),
				Map.of("password", folioTenant.getLoginPassword())
			);
			tenantToPersist = FolioTenant.sanitiseFolioTenant(folioTenant);
		}else{
			tenantToPersist = folioTenant;
		}

		return Mono.from(folioTenantRepository.existsById(gen_id))
				.flatMap(doesItExist -> Mono.from(doesItExist ?
					folioTenantRepository.findById(gen_id) :
					folioTenantRepository.save(tenantToPersist)
				));
    }

		private void handleLoginPasswordVaultSecret(String key, Map<String, Object> secret) {
				VaultSecret password = vaultProvider.readSecret(key);
			if (password.data().isEmpty() && secret != null) {
					// Assume no password present in vault so we need to create one
					//Not returning anything, assuming it shouldn't need to
					vaultProvider.createSecret(key, secret);
					// Now that we've created the password secret based on the ID we now need to remove it from the tenant
				}
			}
}
