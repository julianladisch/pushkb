package com.k_int.pushKb.services;

import java.lang.Boolean;

import com.k_int.pushKb.model.Destination;

import reactor.core.publisher.Mono;

// Generic interface for all interactions with the API for a give destination type
public interface DestinationApiService<T extends Destination> {
  Mono<Boolean> testMethod(T destination);
}
