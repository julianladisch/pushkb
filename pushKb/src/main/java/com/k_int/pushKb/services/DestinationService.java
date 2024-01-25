package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public class DestinationService<T extends Destination> {
  protected final DestinationRepository<T> destinationRepository;

	public DestinationService(
    DestinationRepository<T> destinationRepository
  ) {
    this.destinationRepository = destinationRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<T> findById( UUID id ) {
    return destinationRepository.findById(id);
  }
}
