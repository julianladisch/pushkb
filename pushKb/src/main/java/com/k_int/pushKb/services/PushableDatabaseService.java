package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Pushable;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public interface PushableDatabaseService<T extends Pushable>  {
  
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
  public Publisher<T> ensurePushable( T psh );

  @NonNull
  @Transactional
  public Publisher<T> getFeed();

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<T> update( T psh );

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> complete( T psh );
}