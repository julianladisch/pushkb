package com.k_int.pushKb.interactions.folio.storage;


import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

// Place for any FolioDestination specific logic
@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioDestinationRepository extends DestinationRepository<FolioDestination> {

  // Specific Folio tenant ensureDestination (Needs generateUUID cos we've decided to use UUID5)
  @NonNull
  @SingleResult
  @Transactional
	@Join("folioTenant")
  default Publisher<FolioDestination> ensureDestination( FolioDestination dest ) {
    UUID gen_id = FolioDestination.generateUUID(
      dest.getFolioTenant(),
      dest.getDestinationType()
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

  // Unique up to baseUrl
  @NonNull
  @SingleResult
	@Join("folioTenant")
  Publisher<FolioDestination> findById(@Nullable UUID id);

  @NonNull
  @SingleResult
	@Join("folioTenant")
  Publisher<FolioDestination> save(@Valid @NotNull FolioDestination dest);

  @NonNull
  @Transactional
  @Join("folioTenant")
  Publisher<FolioDestination> list();

  @NonNull
  @SingleResult
	@Join("folioTenant")
  Publisher<FolioDestination> update(@Valid @NotNull FolioDestination dest);

  @NonNull
  @Transactional
  @Join("folioTenant")
  Publisher<Page<FolioDestination>> findAll(Pageable pageable);

  @Override // I don't love that this has to be overwritten in every repository.
  default UUID generateUUIDFromObject(FolioDestination obj) {
    return FolioDestination.generateUUIDFromDestination(obj);
  }
}
