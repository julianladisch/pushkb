package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public interface DestinationDatabaseService<T extends Destination> {
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Destination> findById( UUID id );

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id );

  @NonNull
  @Transactional
  public Publisher<? extends Destination> list();

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Destination> ensureDestination( T destination );
}
