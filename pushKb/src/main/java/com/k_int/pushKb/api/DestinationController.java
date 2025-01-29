package com.k_int.pushKb.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.k_int.pushKb.services.DestinationService;

// FIXME This should be Auth protected
@Controller("/destinations")
@Slf4j
public class DestinationController {
  @Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
  public Mono<Map<String, Object>> getImplementors() {
    return Flux.fromIterable(DestinationService.destinationImplementors)
      .map(clazz -> clazz.toString())
      .collectList()
      .map(implementingArray -> {
        return Map.of("implementers", implementingArray);
      });
  }

  // Destinations themselves will be managed via "interactions/<interaction>/api/<interaction>Controller"
}
