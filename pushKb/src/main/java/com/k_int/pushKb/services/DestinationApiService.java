package com.k_int.pushKb.services;

import java.lang.Boolean;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.model.Destination;

import io.micronaut.json.tree.JsonNode;
import reactor.core.publisher.Mono;

// Generic interface for all interactions with the API for a give destination type
public interface DestinationApiService<T extends Destination> {
  public Publisher<? extends DestinationClient<T>> getClient(T destination);

  Mono<Boolean> push(T destination, DestinationClient<T> client, JsonNode json);
}
