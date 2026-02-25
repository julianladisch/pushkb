package com.k_int.pushKb.services;

import com.k_int.pushKb.crud.CrudDatabaseService;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

/**
 * Service interface for managing Destination entities.
 * <p>
 * This service provides idempotent "ensure" operations suitable for deterministic
 * resource management where the identity of the destination is derived from its properties.
 * </p>
 */
public interface DestinationDatabaseService<T extends Destination> extends CrudDatabaseService<T> {

	/**
	 * @return A stream of all destinations managed by this service.
	 */
	@NonNull
	@Transactional
	Publisher<? extends Destination> list();

	/**
	 * Implements a "Get or Create" (Idempotent Insert) pattern.
	 * <p>
	 * This method checks for the existence of a destination based on its deterministic ID.
	 * <ul>
	 * <li>If the destination exists: Returns the <b>existing</b> record from storage.
	 * Any differences in the provided {@code destination} object are ignored.</li>
	 * <li>If it does not exist: Persists the provided {@code destination} as a new record.</li>
	 * </ul>
	 * </p>
	 *
	 * @param destination The destination to ensure.
	 * @return A publisher emitting the persisted or existing destination.
	 */
	@NonNull
	@SingleResult
	@Transactional
	Publisher<? extends Destination> ensureDestination( T destination );
}
