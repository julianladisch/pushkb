package com.k_int.pushKb.services;

import java.util.Set;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.model.Destination;

import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;

// This is the place to do any generic destination MODEL stuff, with specific model work being handled by the repositories
@Singleton
public class DestinationService {
  // static to return all destination implementing classes -- not sure if this belongs here tbh
  public static final Set<Class<? extends Destination>> destinationImplementors = Set.of(FolioDestination.class);

  private final BeanContext beanContext;
  public DestinationService ( BeanContext beanContext ) {

    this.beanContext = beanContext;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationDatabaseService<Destination> getDestinationServiceForDestinationType( Class<T> destinationType ) {
    return (DestinationDatabaseService<Destination>) beanContext.getBean( Argument.of(DestinationDatabaseService.class, destinationType) ); // Use argument specify core type plus any generic...
  }

  // Replaced with service calls, one more layer of abstraction
/*   @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationRepository<Destination> getRepositoryForDestinationType( Class<T> destinationType ) {
    return (DestinationRepository<Destination>) beanContext.getBean( Argument.of(DestinationRepository.class, destinationType) ); // Use argument specify core type plus any generic...
  } */

  @SuppressWarnings("unchecked")
  protected <T extends Destination> DestinationApiService<Destination> getApiServiceForDestinationType( Class<T> destinationType) {
    return (DestinationApiService<Destination>) beanContext.getBean( Argument.of(DestinationApiService.class, destinationType) ); // Use argument specify core type plus any generic...
  }

  public Publisher<? extends Destination> findById(Class<? extends Destination> type, UUID id ) {
    return getDestinationServiceForDestinationType(type).findById(id);
  }

  public Publisher<Boolean> existsById( Class<? extends Destination> type, UUID id  ) {
    return getDestinationServiceForDestinationType(type).existsById(id);
  }

  // FIXME double check this ensure works as expected
  public Publisher<? extends Destination> ensureDestination(Destination dest ) {
    return getDestinationServiceForDestinationType(dest.getClass()).ensureDestination(dest);
  }

  public Publisher<? extends DestinationClient<Destination>> getClient(Destination dest) {
    return getApiServiceForDestinationType(dest.getClass()).getClient(dest);
  }

  // I don't love this, but I don't want to have to create new clients for each push
  public Publisher<Boolean> push(Destination destination, DestinationClient<Destination> client, JsonNode json) {
    return getApiServiceForDestinationType(client.getDestinationClass()).push(destination, client, json);
  }
}
