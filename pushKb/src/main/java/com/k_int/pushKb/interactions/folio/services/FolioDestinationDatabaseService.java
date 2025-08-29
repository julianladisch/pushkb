package com.k_int.pushKb.interactions.folio.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.folio.storage.FolioDestinationRepository;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.services.DestinationDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class FolioDestinationDatabaseService implements DestinationDatabaseService<FolioDestination> {
  private final FolioDestinationRepository folioDestinationRepository;
  private final FolioTenantDatabaseService folioTenantDatabaseService;

	public FolioDestinationDatabaseService(
    FolioDestinationRepository folioDestinationRepository,
    FolioTenantDatabaseService folioTenantDatabaseService
  ) {
    this.folioDestinationRepository = folioDestinationRepository;
    this.folioTenantDatabaseService = folioTenantDatabaseService;
	}

	public FolioDestinationRepository getRepository() {
		return folioDestinationRepository;
	}

  @NonNull
  @Transactional
  public Publisher<FolioDestination> list() {
    return folioDestinationRepository.list();
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<FolioDestination> ensureDestination( FolioDestination destination ) {
    return Mono.from(folioTenantDatabaseService.ensureFolioTenant(destination.getFolioTenant())) // Ensure the folio tenant first, then the dest as a whole
      .flatMap(folioTenant -> {
        // Use the folioTenant that was ensured, so as not to attempt to create any folioTenant unnecessarily
        destination.setFolioTenant(folioTenant);
        return Mono.from(folioDestinationRepository.ensureDestination(destination));
      });
    }
}
