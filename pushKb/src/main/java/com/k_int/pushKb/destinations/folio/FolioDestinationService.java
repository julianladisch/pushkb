package com.k_int.pushKb.destinations.folio;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.services.DestinationService;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class FolioDestinationService extends DestinationService<FolioDestination> {
  public FolioDestinationService(
    DestinationRepository<FolioDestination> destinationRepository
  ) {
    super(destinationRepository);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<FolioDestination> ensureDestination( FolioDestination dest ) {
    UUID gen_id = FolioDestination.generateUUID(
      dest.getTenant(),
      dest.getDestinationUrl()
    );

    dest.setId(gen_id);

    return Mono.from(destinationRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          if (doesItExist) {
            return Mono.from(destinationRepository.findById(gen_id));
          }

          return Mono.from(destinationRepository.save(dest));
        });
  }
}
