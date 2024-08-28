package com.k_int.pushKb.interactions;

import java.util.Optional;

import io.micronaut.http.HttpResponse;
import reactor.core.publisher.Mono;

public interface ApiClient {
  <T> Mono<HttpResponse<T>> get(
    String path, // URL path
    Class<T> responseType, // Class to map response body to
    Optional<Class<?>> errorType // Class to map error response to
  );

  <T> Mono<HttpResponse<T>> put(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  );

  <T> Mono<HttpResponse<T>> post(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  );

  <T> Mono<HttpResponse<T>> delete(
    String path,
    Class<T> responseType,
    Optional<Class<?>> errorType
  );
}
