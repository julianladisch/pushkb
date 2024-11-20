package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Pushable;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class PushableService {
  private final BeanContext beanContext;

  public PushableService (
    BeanContext beanContext
    ) {
    this.beanContext = beanContext;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Pushable> PushableDatabaseService<Pushable> getPushableDatabaseServiceForPushableType( Class<T> pushableType ) {
    return (PushableDatabaseService<Pushable>) beanContext.getBean( Argument.of(PushableDatabaseService.class, pushableType) ); // Use argument specify core type plus any generic...
  }


  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Pushable> findById( Class<? extends Pushable> type, UUID id ) {
    return getPushableDatabaseServiceForPushableType(type).findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( Class<? extends Pushable> type, UUID id ) {
    return getPushableDatabaseServiceForPushableType(type).existsById(id);
  }

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> getFeed(Class<? extends Pushable> type ) {
    return getPushableDatabaseServiceForPushableType(type).getFeed();
  }

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> ensurePushable( Pushable psh ) {
    return getPushableDatabaseServiceForPushableType(psh.getClass()).ensurePushable(psh);
  }

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> update( Pushable psh ) {
    return getPushableDatabaseServiceForPushableType(psh.getClass()).update(psh);
  }

  // What to do with a completed pushTask run
  @NonNull
  @Transactional
  public Publisher<Boolean> complete( Pushable psh ) {
    return getPushableDatabaseServiceForPushableType(psh.getClass()).complete(psh);
  }
}
