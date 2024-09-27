package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public interface SourceDatabaseService<T extends Source> {
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<T> findById( UUID id );

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id );

  @NonNull
  @Transactional
  public Publisher<T> list();

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<T> ensureSource( T src );

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<T> saveOrUpdate( T src );

  default Publisher<T> setLastIngestStarted(T src, Instant time) {
    src.setLastIngestStarted(time);
    return saveOrUpdate(src);
  }

  // Overload for "set to now" ease of use
  default Publisher<T> setLastIngestStarted(T src) {
    src.setLastIngestStarted(Instant.now());
    return saveOrUpdate(src);
  }

  default Publisher<T> setLastIngestCompleted(T src, Instant time) {
    src.setLastIngestCompleted(time);
    return saveOrUpdate(src);
  }

  // Overload for "set to now" ease of use
  default Publisher<T> setLastIngestCompleted(T src) {
    src.setLastIngestCompleted(Instant.now());
    return saveOrUpdate(src);
  }
}