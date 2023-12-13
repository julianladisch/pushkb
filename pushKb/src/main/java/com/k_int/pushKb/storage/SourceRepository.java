package com.k_int.pushKb.storage;

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
public interface SourceRepository extends ReactiveStreamsPageableRepository<Source, UUID> {
  @NonNull
	Publisher<Source> findAllBySourceUrl ( String sourceUrl );

  @NonNull
  @SingleResult
	Publisher<Source> findBySourceUrlAndCodeAndSourceType ( String sourceUrl, SourceCode code, SourceType type );

  // Find by relevant data, even from built Source without id.
  @NonNull
  @SingleResult
	default Publisher<Source> findBySourceData ( Source source ) {
    return findBySourceUrlAndCodeAndSourceType(source.getSourceUrl(), source.getCode(), source.getSourceType());
  }

  @NonNull
  @SingleResult
	Publisher<Boolean> existsBySourceUrlAndCodeAndSourceType ( String sourceUrl, SourceCode code, SourceType type );

  @NonNull
  @SingleResult
	default Publisher<Boolean> existsBySourceData ( Source source ) {
    return existsBySourceUrlAndCodeAndSourceType(source.getSourceUrl(), source.getCode(), source.getSourceType());
  }

  @NonNull
	Publisher<Source> findAllByCodeAndSourceType ( SourceCode code, SourceType type );

  @NonNull
	Publisher<Source> findAllByCode ( SourceCode code );

  @NonNull
	Publisher<Source> findAllBySourceType ( SourceType type );

  @SingleResult
  @NonNull
  default Publisher<Source> saveOrUpdate(@Valid @NotNull Source src) {
    return Mono.from(this.existsById(src.getId()))
               .flatMap( update -> Mono.from(update ? this.update(src) : this.save(src)) )
    ;
  }
}