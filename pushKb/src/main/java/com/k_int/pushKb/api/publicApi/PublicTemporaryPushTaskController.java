package com.k_int.pushKb.api.publicApi;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Mono;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.services.PushableService;

// This will be a non-authenticated endpoint,
// (for v1) able only to create temporary pushTasks from existing ones
// and not access or change ANY data
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/public/temporarypushtask")
@Slf4j
public class PublicTemporaryPushTaskController {
	private final PushableService pushableService;

	public PublicTemporaryPushTaskController(
		PushableService pushableService
	) {
		this.pushableService = pushableService;
	}

	// TODO add @Body binding shape
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
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
}
