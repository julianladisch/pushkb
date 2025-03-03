package com.k_int.pushKb.api.publicApi;

import reactor.core.publisher.Mono;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;

import io.micronaut.json.tree.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import com.k_int.proteus.ComponentSpec;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.proteus.ProteusService;
import com.k_int.pushKb.services.PushableService;

// This will be a non-authenticated endpoint,
// (for v1) able only to create temporary pushTasks from existing ones
// and not access or change ANY data
@Controller("/public")
@Slf4j
public class PublicController {
  private final PushableService pushableService;

  private final ProteusService proteusService;

  public PublicController(
    PushableService pushableService,
    ProteusService proteusService
  ) {
    this.pushableService = pushableService;
    this.proteusService = proteusService;
	}

  // TODO add @Body binding shape
  @Post(uri = "/temporarypushtask", produces = MediaType.APPLICATION_JSON)
  public Mono<MutableHttpResponse<Map<String, Object>>> temporaryPushTask(
    String pushTaskId,
    @Nullable String filterContext
  ) {
    // FIXME If we don't find the push task by id we will return 404 right now, which isn't super helpful
    return Mono.from(pushableService.findById(PushTask.class, UUID.fromString(pushTaskId)))
      .map(PushTask.class::cast)
			.switchIfEmpty(Mono.error(new RuntimeException(String.format("Could not find PushTask with ID: %s", pushTaskId))))
			// FIXME we should be passing Mono error here not mono empty ??
			.flatMap(pushTask -> Mono.from(pushableService.ensurePushable(
        TemporaryPushTask.builder()
          .pushTask(pushTask)
          .filterContext(filterContext)
          .build()
      )))
      .map(TemporaryPushTask.class::cast)
			.switchIfEmpty(Mono.error(new RuntimeException(String.format("Could not create TemporaryPushTask(pushTask: %s, filterContext: %s), one already exists", pushTaskId, filterContext))))
			// TODO return something more useful than what was sent down.
      .map(temporaryPushTask -> {
				return HttpResponse.created(Map.of(
					"pushTaskId", pushTaskId,
					"filterContext", (filterContext != null ? filterContext : "No filter context provided"),
					"temporaryPushTaskId", temporaryPushTask.getId().toString()
				));
			});
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
