package com.k_int.pushKb.storage;


import java.util.UUID;

import com.k_int.pushKb.model.Error;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;

public interface ErrorRepository<T> extends ReactiveStreamsPageableRepository<Error<T>, UUID> {
  @NonNull
  @SingleResult
  Publisher<? extends Error<T>> addError(T owner, String code, String message);
}
