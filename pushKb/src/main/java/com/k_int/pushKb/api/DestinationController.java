package com.k_int.pushKb.api;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.k_int.pushKb.services.DestinationService;

@Controller("/destinations")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class DestinationController {
  @Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
  public Mono<Map<String, Object>> getImplementors() {
    return Flux.fromIterable(DestinationService.destinationImplementors)
      .map(Class::toString)
      .collectList()
      .map(implementingArray -> Map.of("implementers", implementingArray));
  }

  // Destinations themselves will be managed via "interactions/<interaction>/api/<interaction>Controller"
}
