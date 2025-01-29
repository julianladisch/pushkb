package com.k_int.pushKb.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.k_int.pushKb.services.SourceService;

// FIXME This should be Auth protected
@Controller("/sources")
@Slf4j
public class SourceController {
  @Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
  public Mono<Map<String, Object>> getImplementors() {
    return Flux.fromIterable(SourceService.sourceImplementers)
      .map(clazz -> clazz.toString())
      .collectList()
      .map(implementingArray -> {
        return Map.of("implementers", implementingArray);
      });
  }

  // Sources themselves will be managed via "interactions/<interaction>/api/<interaction>Controller"
}
