package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.Source;

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
public interface DestinationSourceLinkRepository extends ReactiveStreamsPageableRepository<DestinationSourceLink, UUID> {
  @NonNull
  @SingleResult
	Publisher<DestinationSourceLink> findByDestinationAndSourceAndTransform ( Destination destination, Source source, String transform );

  // Find by relevant data.
  @NonNull
  @SingleResult
	default Publisher<DestinationSourceLink> findByDSLData ( DestinationSourceLink dsl ) {
    return findByDestinationAndSourceAndTransform(
      dsl.getDestination(),
      dsl.getSource(),
      dsl.getTransform()
    );
  }
  
  @NonNull
  @SingleResult
	Publisher<Boolean> existsByDestinationAndSourceAndTransform ( Destination destination, Source source, String transform );

  @NonNull
  @SingleResult
	default Publisher<Boolean> existsByDSLData ( DestinationSourceLink dsl ) {
    return existsByDestinationAndSourceAndTransform(
      dsl.getDestination(),
      dsl.getSource(),
      dsl.getTransform()
    );
  }
  
}