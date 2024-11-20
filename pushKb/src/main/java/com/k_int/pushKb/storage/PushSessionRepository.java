package com.k_int.pushKb.storage;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

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
public interface PushSessionRepository extends ReactiveStreamsPageableRepository<PushSession, UUID> {
  @NonNull
  @SingleResult
  Publisher<PushSession> findById(@Nullable UUID id);

  // FIXME these might need to be find by Pushable (UUID)...
  // Do we need type and id for these methods?
  @NonNull
  Publisher<PushSession> findByPushableIdAndCreated(UUID pid, Instant created);

  @NonNull
  Publisher<PushSession> findAllByPushableId(UUID pid);

  @NonNull
  Publisher<PushSession> findAllByPushableIdAndCreatedGreaterThanEqualsAndCreatedLessThan(UUID pid, Instant from, Instant to);
}