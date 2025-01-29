package com.k_int.pushKb.crud;

import java.util.UUID;

import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;

public interface ReactiveStreamsPageableRepositoryUUID5<T, ID> extends ReactiveStreamsPageableRepository<T, ID> {
  UUID generateUUIDFromObject(T obj);
}
