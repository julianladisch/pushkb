package com.k_int.pushKb.core.generic.storage;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.core.generic.model.Generic;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import reactor.core.publisher.Mono;

public interface GenericRepository<T extends Generic> {
  @NonNull
	@SingleResult
	Publisher<? extends T> save(@Valid @NotNull @NonNull T t);

	@NonNull
	@SingleResult
	Publisher<? extends T> update(@Valid @NotNull @NonNull T t);

	@NonNull
	@SingleResult
	Publisher<T> findById(@NotNull UUID id);

	@NonNull
	Publisher<T> queryAll();

	@NonNull
	@SingleResult
	Publisher<Page<T>> queryAll(Pageable page);

  @SingleResult
	Publisher<Void> delete(UUID id);

	@SingleResult
	@NonNull
	default Publisher<T> saveOrUpdate(@Valid @NotNull @NonNull T t) {
		return Mono.from(this.existsById(t.getId()))
				.flatMap(update -> Mono.from(update ? this.update(t) : this.save(t)));
	}

  @NonNull
	@SingleResult
	Publisher<Boolean> existsById(@NonNull UUID id);
}
