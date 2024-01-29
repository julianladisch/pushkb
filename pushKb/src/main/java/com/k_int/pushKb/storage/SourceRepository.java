package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Source;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface SourceRepository<T extends Source> extends ReactiveStreamsPageableRepository<T, UUID> {
  @SingleResult
  @NonNull
  default Publisher<T> saveOrUpdate(@Valid @NotNull T src) {
    return Mono.from(this.existsById(src.getId()))
               .flatMap( update -> Mono.from(update ? this.update(src) : this.save(src)) );
  }

  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);
  Publisher<T> findById(@Nullable UUID id);

  // If we weren't using UUID5 for each Source type, we could instead have a default implementation here
  @NonNull
  @SingleResult
  Publisher<T> ensureSource( T src );
}