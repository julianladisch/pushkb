package com.k_int.pushKb.api;

import com.k_int.pushKb.model.responses.SourceImplementersDTO;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.services.SourceService;

@Controller("/sources")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class SourceController implements SourceApi {
  @Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
  public Mono<SourceImplementersDTO> getImplementers() {
    return Flux.fromIterable(SourceService.sourceImplementers)
      .map(Class::toString)
      .collectList()
      .map(implementingArray -> SourceImplementersDTO.builder().implementers(implementingArray).build());
  }

  // Sources themselves will be managed via "interactions/<interaction>/api/<interaction>Controller"
}
