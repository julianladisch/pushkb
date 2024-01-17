package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationType;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface DestinationRepository extends ReactiveStreamsPageableRepository<Destination, UUID> {
  @NonNull
  @SingleResult
	Publisher<Destination> findByDestinationUrlAndDestinationType ( String destinationUrl, DestinationType type );

  // Find by relevant data, even from built Source without id.
  @NonNull
  @SingleResult
	default Publisher<Destination> findByDestinationData ( Destination dest ) {
    return findByDestinationUrlAndDestinationType(dest.getDestinationUrl(), dest.getDestinationType());
  }
  
  @NonNull
  @SingleResult
	Publisher<Boolean> existsByDestinationUrlAndDestinationType ( String destinationUrl, DestinationType type );

  @NonNull
  @SingleResult
	default Publisher<Boolean> existsByDestinationData ( Destination dest ) {
    return existsByDestinationUrlAndDestinationType(dest.getDestinationUrl(), dest.getDestinationType());
  }
  
}