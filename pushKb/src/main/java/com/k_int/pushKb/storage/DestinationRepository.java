package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Destination;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

public interface DestinationRepository<T extends Destination> extends ReactiveStreamsPageableRepository<T, UUID> {
  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);

  @NonNull
  @SingleResult
  Publisher<T> findById(@Nullable UUID id); 
}
