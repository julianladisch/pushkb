package com.k_int.pushKb.api.publicApi;

import reactor.core.publisher.Mono;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import io.micronaut.json.tree.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import com.k_int.proteus.ComponentSpec;
import com.k_int.pushKb.proteus.ProteusService;

// This will be a non-authenticated endpoint,
// (for v1) able only to create temporary pushTasks from existing ones
// and not access or change ANY data
@Controller("/public")
@Slf4j
public class PublicController {

  private final ProteusService proteusService;

  public PublicController(
    ProteusService proteusService
  ) {
    this.proteusService = proteusService;
	}

  // FIXME This shouldn't be a public API call -- should be protected
  @Post(uri = "/proteus", produces = MediaType.APPLICATION_JSON)
  public Mono<Map<String, JsonNode>> proteus(
    JsonNode record,
    JsonNode schema
  ) {
    ComponentSpec<JsonNode> spec = proteusService.loadSpec(schema);

    try {
      JsonNode output = proteusService.convert(spec, record);
      return Mono.just(Map.of(
        "input", record,
        "output", output
      ));
    } catch (Exception e) {
      log.error("Something went wrong in proteus convert", e);
      return Mono.empty();
    }
  }
}
