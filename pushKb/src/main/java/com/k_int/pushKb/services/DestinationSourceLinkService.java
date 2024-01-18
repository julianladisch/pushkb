package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.storage.DestinationSourceLinkRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class DestinationSourceLinkService {
  private final DestinationSourceLinkRepository destinationSourceLinkRepository;
  private final DestinationService destinationService;
  private final SourceService sourceService;
	public DestinationSourceLinkService(
    DestinationSourceLinkRepository destinationSourceLinkRepository,
    DestinationService destinationService,
    SourceService sourceService
  ) {
    this.destinationSourceLinkRepository = destinationSourceLinkRepository;
    this.destinationService = destinationService;
    this.sourceService = sourceService;
	}
  
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<DestinationSourceLink> ensureDestinationSourceLink( DestinationSourceLink dsl ) {
    // Firstly we need to ensure service and destination from passed data
    // TODO do we want ensureDSL to have power to create sources and destinations?
    return Mono.from(destinationService.ensureDestination(dsl.getDestination()))
    .zipWith(Mono.from(sourceService.ensureSource(dsl.getSource())))
    .flatMap(tuple -> {
      Destination destination = tuple.getT1();
      Source source = tuple.getT2();
      UUID gen_id = DestinationSourceLink.generateUUID(
        destination,
        source,
        dsl.getTransform()
      );

      // Set up new DSL so we're definitely not passing the one from the parameters
      DestinationSourceLink new_dsl = dsl.toBuilder()
                                        .id(gen_id)
                                        .destination(destination)
                                        .source(source)
                                        .build();

      return Mono.from(destinationSourceLinkRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            destinationSourceLinkRepository.findById(gen_id) :
            destinationSourceLinkRepository.save(new_dsl)
          );
      });
    });
      
    /* 
    
    return Mono.from(destinationSourceLinkRepository.existsByDSLData(dsl))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            destinationSourceLinkRepository.findByDSLData(dsl) :
            destinationSourceLinkRepository.save(dsl)
          );
        }); */
  }
}
