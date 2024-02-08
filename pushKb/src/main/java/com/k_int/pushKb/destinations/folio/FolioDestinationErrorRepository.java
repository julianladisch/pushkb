package com.k_int.pushKb.destinations.folio;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioDestinationErrorRepository extends ReactiveStreamsPageableRepository<FolioDestinationError, UUID> {
  
  @NonNull
  @SingleResult
  default Publisher<FolioDestinationError> addError(FolioDestination owner, String code, String message) {
    FolioDestinationError fde = FolioDestinationError.builder()
			.owner(owner)
			.code(code)
			.message(message)
      .build();

		return save(fde);
  }

  public Publisher<Void> deleteAllByOwnerAndCode(FolioDestination owner, String code);
  public Publisher<Boolean> existsByOwnerAndCode(FolioDestination owner, String code);
}
