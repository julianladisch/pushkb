package com.k_int.pushKb.api;

import com.k_int.pushKb.model.responses.DestinationImplementersDTO;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.services.DestinationService;

@Controller("/destinations")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class DestinationController implements DestinationApi {
  @Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
  public Mono<DestinationImplementersDTO> getImplementers() {
    return Flux.fromIterable(DestinationService.destinationImplementors)
      .map(Class::toString)
      .collectList()
      .map(implementingArray -> DestinationImplementersDTO.builder().implementers(implementingArray).build());
  }

  // Destinations themselves will be managed via "interactions/<interaction>/api/<interaction>Controller"
}
