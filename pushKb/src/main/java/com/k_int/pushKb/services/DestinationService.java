package com.k_int.pushKb.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationType;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;

import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Singleton
public class DestinationService {
  private final DestinationRepository destinationRepository;
	public DestinationService(
    DestinationRepository destinationRepository
  ) {
    this.destinationRepository = destinationRepository;
	}

  // Must be protected at least to allow AOP annotations.
  // Adding this method gives us something to hang the transaction from. We also use the @Valid annotation
  // to validate the source record before we save it.
  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  protected Publisher<Destination> saveSource ( @NonNull @Valid Destination d ) {
  	return destinationRepository.save(d);
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
    return Mono.from(destinationRepository.existsByDestinationData(dest))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            destinationRepository.findByDestinationData(dest) :
            destinationRepository.save(dest)
          );
        });
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Destination> findByDestinationUrlAndDestinationType( String destinationUrl, DestinationType type ) {
    return destinationRepository.findByDestinationUrlAndDestinationType(destinationUrl, type);
  }
}
