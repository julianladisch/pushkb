package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

// This is the place to do any generic destinations stuff, with specific work being handled by the repositories
@Singleton
public class DestinationService {
  private final BeanContext beanContext;
  public DestinationService ( BeanContext beanContext ) {

    this.beanContext = beanContext;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationRepository<T> getRepositoryForDestinationType( Class<T> destinationType ) {
    return (DestinationRepository<T>) beanContext.getBean( Argument.of(DestinationRepository.class, destinationType) ); // Use argument specify core type plus any generic...
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Destination> Publisher<T> findById( UUID id, Class<T> type ) {
    return getRepositoryForDestinationType(type).findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Destination> Publisher<Boolean> existsById( UUID id, Class<T> type ) {
    return getRepositoryForDestinationType(type).existsById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Destination> Publisher<T> ensureDestination( T dest, Class<T> type ) {
  //public <T extends Destination> Publisher<T> ensureDestination( T dest ) {
    return getRepositoryForDestinationType(type).ensureDestination(dest);
  }
}
