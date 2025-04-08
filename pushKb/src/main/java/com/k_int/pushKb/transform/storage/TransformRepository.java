package com.k_int.pushKb.transform.storage;

import com.k_int.pushKb.crud.ReactiveStreamsPageableRepositoryUUID5;

import java.util.UUID;

import com.k_int.pushKb.transform.model.Transform;
import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface TransformRepository<T extends Transform> extends ReactiveStreamsPageableRepositoryUUID5<T, UUID> {
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

	@NonNull
	Publisher<T> list();

	@Override // I don't love that this has to be overwritten in every repository.
	default UUID generateUUIDFromObject(T obj) {
		return Transform.generateUUIDFromTransform(obj);
	}
}
