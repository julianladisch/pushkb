package com.k_int.pushKb.services;

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
  public Publisher<? extends Source> findById( UUID id );

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id );

  @NonNull
  @Transactional
  public Publisher<? extends Source> list();

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> ensureSource( T src );
}