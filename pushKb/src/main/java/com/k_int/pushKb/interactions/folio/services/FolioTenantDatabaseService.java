package com.k_int.pushKb.interactions.folio.services;


import java.util.UUID;

import com.k_int.pushKb.crud.CrudDatabaseService;
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
  public FolioTenantDatabaseService(
    FolioTenantRepository folioTenantRepository
  ) {
    this.folioTenantRepository = folioTenantRepository;
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

      return Mono.from(folioTenantRepository.existsById(gen_id))
        .flatMap(doesItExist -> Mono.from(doesItExist ?
					folioTenantRepository.findById(gen_id) :
					folioTenantRepository.save(folioTenant)
				));
    }
}
