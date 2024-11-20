package com.k_int.pushKb.api;

import reactor.core.publisher.Mono;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;

import com.k_int.pushKb.services.PushableService;

// This will be a non-authenticated endpoint,
// (for v1) able only to create temporary pushTasks from existing ones
// and not access or change ANY data
@Controller("/public")
@Slf4j
public class PublicController {
  private final PushableService pushableService;

  public PublicController(
    PushableService pushableService
  ) {
    this.pushableService = pushableService;
	}

  @Post(uri = "/temporaryPushTask", produces = MediaType.APPLICATION_JSON)
  public Mono<Map<String, Object>> temporaryPushTask(
    String pushTaskId,
    @Nullable String filterContext
  ) {
    return Mono.from(pushableService.findById(PushTask.class, UUID.fromString(pushTaskId)))
      .map(PushTask.class::cast)
      .flatMap(pushTask -> Mono.from(pushableService.ensurePushable(
        TemporaryPushTask.builder()
          .pushTask(pushTask)
          .filterContext(filterContext)
          .build()
      )))
      .map(TemporaryPushTask.class::cast)
      // TODO return something more useful than what was sent down.
      .map(temporaryPushTask -> Map.of(
        "PushTask id", pushTaskId,
        "Filter context", (filterContext != null ? filterContext : "No filter context provided"),
        "TemporaryPushTask id", temporaryPushTask.getId().toString()
      ));
  }
}
