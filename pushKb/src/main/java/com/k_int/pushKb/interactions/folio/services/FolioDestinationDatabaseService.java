package com.k_int.pushKb.interactions.folio.services;

import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.folio.storage.FolioDestinationRepository;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.services.DestinationDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
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

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<FolioDestination> findById(UUID id ) {
    return folioDestinationRepository.findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id ) {
    return folioDestinationRepository.existsById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Long> deleteById( UUID id ) {
    return folioDestinationRepository.deleteById(id);
  }

  @NonNull
  @Transactional
  public Publisher<List<FolioDestination>> findAll(Pageable pageable) {
    return Mono.from(folioDestinationRepository.findAll(pageable)).map(Page::getContent);
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
    };
  
  @Transactional
  @SingleResult
  public Publisher<FolioDestination> update ( @NonNull @Valid FolioDestination dest ) {
    return folioDestinationRepository.update(dest);
  }

  @Transactional
  @SingleResult
  public Publisher<FolioDestination> save ( @NonNull @Valid FolioDestination dest ) {
    return folioDestinationRepository.save(dest);
  }
}
