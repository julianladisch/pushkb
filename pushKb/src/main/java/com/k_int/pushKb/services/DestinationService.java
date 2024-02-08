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

// This is the place to do any generic destination MODEL stuff, with specific model work being handled by the repositories
@Singleton
public class DestinationService {
  private final BeanContext beanContext;
  public DestinationService ( BeanContext beanContext ) {

    this.beanContext = beanContext;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationRepository<Destination> getRepositoryForDestinationType( Class<T> destinationType ) {
    return (DestinationRepository<Destination>) beanContext.getBean( Argument.of(DestinationRepository.class, destinationType) ); // Use argument specify core type plus any generic...
  }

  @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationApiService<Destination> getApiServiceForDestinationType( Class<T> destinationType) {
    return (DestinationApiService<Destination>) beanContext.getBean( Argument.of(DestinationApiService.class, destinationType) ); // Use argument specify core type plus any generic...
  }

  public Publisher<? extends Destination> findById(Class<? extends Destination> type, UUID id ) {
    return getRepositoryForDestinationType(type).findById(id);
  }

  public Publisher<Boolean> existsById( Class<? extends Destination> type, UUID id  ) {
    return getRepositoryForDestinationType(type).existsById(id);
  }

  // FIXME double check this ensure works as expected
  public Publisher<? extends Destination> ensureDestination(Destination dest ) {
    return getRepositoryForDestinationType(dest.getClass()).ensureDestination(dest);
  }

  // FIXME this is just a test rn
  public Publisher<Boolean> testMethod(Destination dest) {
    return getApiServiceForDestinationType(dest.getClass()).testMethod(dest);
  }
}
