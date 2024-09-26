package com.k_int.pushKb.storage;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.PushSession;
import com.k_int.pushKb.model.PushTask;

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
public interface PushSessionRepository extends ReactiveStreamsPageableRepository<PushSession, UUID> {
  @NonNull
  @SingleResult
  Publisher<PushSession> findById(@Nullable UUID id);

  @NonNull
  Publisher<PushSession> findByPushTaskAndCreated(PushTask pt, Instant created);

  @NonNull
  Publisher<PushSession> findAllByPushTask(PushTask pt);

  // from INCLUSIVE, to EXCLUSIVE
  @NonNull
  Publisher<PushSession> findAllByPushTaskAndCreatedGreaterThanEqualsAndCreatedLessThan(PushTask pt, Instant from, Instant to);
}