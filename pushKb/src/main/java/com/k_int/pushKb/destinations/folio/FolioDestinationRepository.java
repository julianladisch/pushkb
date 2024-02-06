package com.k_int.pushKb.destinations.folio;


import java.util.Optional;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

// Place for any FolioDestination specific logic
@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioDestinationRepository extends DestinationRepository<FolioDestination> {
  @NonNull
  @SingleResult
  @Transactional
  default Publisher<FolioDestination> ensureDestination( FolioDestination dest ) {
    UUID gen_id = FolioDestination.generateUUID(
      dest.getTenant(),
      dest.getDestinationUrl()
    );

    dest.setId(gen_id);

    return Mono.from(existsById(gen_id))
        .flatMap(doesItExist -> {
          if (doesItExist) {
            return Mono.from(findById(gen_id));
          }

          return Mono.from(save(dest));
        });
  }
}
