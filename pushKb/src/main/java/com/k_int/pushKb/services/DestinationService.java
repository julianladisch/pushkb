package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationType;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class DestinationService {
  private final DestinationRepository destinationRepository;
	public DestinationService(
    DestinationRepository destinationRepository
  ) {
    this.destinationRepository = destinationRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Destination> findById( UUID id ) {
    return destinationRepository.findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Destination> ensureDestination( String destinationUrl, DestinationType type ) {
    Destination dest = Destination.builder()
                       .destinationUrl(destinationUrl)
                       .destinationType(type)
                       .build();

    return ensureDestination(dest);
  }
  
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Destination> ensureDestination( Destination dest ) {
    UUID gen_id = Destination.generateUUID(
      dest.getDestinationType(),
      dest.getDestinationUrl()
    );

    // Set up new Destination so we're definitely not passing the one from the parameters
    Destination new_dest = dest.toBuilder()
                               .id(gen_id)
                               .build();

    return Mono.from(destinationRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            destinationRepository.findById(gen_id) :
            destinationRepository.save(new_dest)
          );
        });
  }
}
