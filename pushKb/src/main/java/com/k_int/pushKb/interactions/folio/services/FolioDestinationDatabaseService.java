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

/**
 * Implementation of {@link DestinationDatabaseService} specifically for FOLIO instances.
 * <p>
 * This service handles the orchestration of {@link FolioDestination} and its
 * required {@link com.k_int.pushKb.interactions.folio.model.FolioTenant}. It ensures that the underlying tenant configuration
 * (including Vault-stored credentials) exists before finalizing the destination.
 * </p>
 */
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

	/**
	 * Ensures the existence of a FolioDestination and its associated FolioTenant.
	 * <p>
	 * <b>Behavior:</b>
	 * <ol>
	 * <li>Calls {@code folioTenantDatabaseService.ensureFolioTenant} to reconcile the tenant first.</li>
	 * <li>Associates the returned tenant (new or existing) with the destination.</li>
	 * <li>Delegates to the repository to return the existing destination or create a new one.</li>
	 * </ol>
	 * <b>Note:</b> This method does NOT perform updates. If a destination already exists
	 * with the same ID but different parameters, the existing database state is preserved.
	 * </p>
	 *
	 * @param destination The Folio destination configuration.
	 * @return A publisher emitting the existing or newly created destination.
	 */
	@NonNull
	@SingleResult
	@Transactional
	public Publisher<FolioDestination> ensureDestination( FolioDestination destination ) {
		return Mono.from(folioTenantDatabaseService.ensureFolioTenant(destination.getFolioTenant()))
			.flatMap(folioTenant -> {
				destination.setFolioTenant(folioTenant);
				return Mono.from(folioDestinationRepository.ensureDestination(destination));
			});
	}
}
