package com.k_int.pushKb.api.publicApi;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Mono;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.http.annotation.Controller;

import lombok.extern.slf4j.Slf4j;

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
public class PublicTemporaryPushTaskController implements PublicTemporaryPushTaskApi {
	private final PushableService pushableService;

	public PublicTemporaryPushTaskController(
		PushableService pushableService
	) {
		this.pushableService = pushableService;
	}

	public Mono<TemporaryPushTask> temporaryPushTask(
		UUID pushTaskId,
		@Nullable String filterContext
	) {
		// FIXME If we don't find the push task by id we will return 404 right now, which isn't super helpful
		return Mono.from(pushableService.findById(PushTask.class, pushTaskId))
			.switchIfEmpty(Mono.error(
				new HttpStatusException(
					HttpStatus.NOT_FOUND,
					"No PushTask found with id: " + pushTaskId
				)
			))
			.map(PushTask.class::cast)
			.flatMap(pushTask -> Mono.from(pushableService.ensurePushable(
				TemporaryPushTask.builder()
					.pushTask(pushTask)
					.filterContext(filterContext)
					.build()
			)))
			.switchIfEmpty(Mono.error(
				new HttpStatusException(
					HttpStatus.CONFLICT,
					String.format("Could not create TemporaryPushTask(pushTask: %s, filterContext: %s), one already exists", pushTaskId, filterContext)
				)
			))
			.map(TemporaryPushTask.class::cast);
	}
}
