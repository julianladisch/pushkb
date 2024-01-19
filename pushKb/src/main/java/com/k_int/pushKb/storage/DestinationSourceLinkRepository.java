package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.Source;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface DestinationSourceLinkRepository extends ReactiveStreamsPageableRepository<DestinationSourceLink, UUID> {
  // Unique up to destination/source/transform
  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);
  
  // Unique up to destination/source/transform
  @NonNull
  @SingleResult
  @Join(value="source")
  @Join(value="destination")
  Publisher<DestinationSourceLink> findById(@Nullable UUID id);

  @NonNull
  @Join(value="source")
  @Join(value="destination")
  Publisher<DestinationSourceLink> findBySourceAndDestination(Source source, Destination destination);

  @NonNull
  @Join(value="source")
  @Join(value="destination")
  Publisher<DestinationSourceLink> listOrderBySourceAndDestinationAndId();
}