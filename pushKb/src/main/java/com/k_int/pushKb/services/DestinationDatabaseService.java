package com.k_int.pushKb.services;

import com.k_int.pushKb.crud.CrudDatabaseService;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public interface DestinationDatabaseService<T extends Destination> extends CrudDatabaseService<T> {
  @NonNull
  @Transactional
  Publisher<? extends Destination> list();

  @NonNull
  @SingleResult
  @Transactional
  Publisher<? extends Destination> ensureDestination( T destination );
}
