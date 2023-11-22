package com.k_int.pushKb.storage;

import com.k_int.pushKb.core.model.HelloWorld;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Singleton
@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional
public interface HelloWorldRepository extends ReactiveStreamsPageableRepository<HelloWorld, UUID> {
  @NonNull
  @SingleResult
  Publisher<HelloWorld> save( @NonNull @Valid  HelloWorld hw );
/*   @SingleResult
  @NonNull
  Publisher<HelloWorld> getOneById( @NonNull UUID id ); */
}
