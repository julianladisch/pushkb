package com.k_int.pushKb.storage;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.PushChunk;
import com.k_int.pushKb.model.PushSession;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface PushChunkRepository extends ReactiveStreamsPageableRepository<PushChunk, UUID> {
  @NonNull
  @SingleResult
  Publisher<PushChunk> findById(@Nullable UUID id);

  @NonNull
  Publisher<PushChunk> findBySessionAndCreated(PushSession ps, Instant created);

  @NonNull
  Publisher<PushChunk> findAllBySession(PushSession ps);

  // from INCLUSIVE, to EXCLUSIVE
  @NonNull
  Publisher<PushChunk> findAllBySessionAndCreatedGreaterThanEqualsAndCreatedLessThan(PushSession ps, Instant from, Instant to);
}