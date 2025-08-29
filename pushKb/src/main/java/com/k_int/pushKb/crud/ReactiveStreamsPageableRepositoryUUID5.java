package com.k_int.pushKb.crud;

import java.util.UUID;

import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;

public interface ReactiveStreamsPageableRepositoryUUID5<T> extends ReactiveStreamsPageableRepository<T, UUID> {
  UUID generateUUIDFromObject(T obj);
}
