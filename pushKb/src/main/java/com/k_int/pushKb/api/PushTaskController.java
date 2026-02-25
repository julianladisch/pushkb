package com.k_int.pushKb.api;

import com.k_int.pushKb.services.PushTaskDatabaseService;
import com.k_int.pushKb.services.PushableService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.model.PushTask;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/pushtasks")
public class PushTaskController extends CrudControllerImpl<PushTask> implements PushTaskApi {
	private final PushTaskDatabaseService databaseService;
	private final PushableService pushableService;
  public PushTaskController(
		PushTaskDatabaseService databaseService,
		PushableService pushableService
	) {
    super(databaseService);

    this.databaseService = databaseService;
		this.pushableService = pushableService;
  }


	@Override
	public Mono<PushTask> post(@Valid @Body PushTask pt) {
		UUID generatedId = databaseService.generateUUIDFromObject(pt);
		pt.setId(generatedId);

		return Mono.from(databaseService.existsById(generatedId))
			.flatMap(exists -> {
				if (exists) {
					return Mono.error(new HttpStatusException(
						HttpStatus.CONFLICT,
						"A PushTask with this identity already exists. " +
							"If you need to change parameters, DELETE the existing task or use a different context."
					));
				}
				return Mono.from(pushableService.ensurePushable(pt))
					.map(PushTask::castFromPushable);
			});
	}

	@Override
	@Hidden
	public Mono<PushTask> put(
		@Parameter UUID id,
		@Valid @Body PushTask pt
	) {
		return Mono.error(new HttpStatusException(
			HttpStatus.METHOD_NOT_ALLOWED,
			"PUT is not supported on PushTasks - POST/DELETE are supported, as well as resetting the pointers"
		));
	}

	@SingleResult
  // Reset pointer endpoint
	public Mono<PushTask> resetPointers(
		@Parameter UUID id
	) {

		return Mono.from(databaseService.findById(id))
			.flatMap(pt -> {
				pt.resetPointer();
				return Mono.from(databaseService.update(pt));
			})
			.switchIfEmpty(Mono.error(new HttpStatusException(
				HttpStatus.NOT_FOUND,
				"No PushTask found with id: " + id
			)));
	}

	// We need to use pushableService here to make sure that side effects happen as expected
	// Such as DutyCycleTask removal etc.
	@Override
	public Mono<Void> delete(
		@Parameter UUID id
	) {
		return Mono.from(pushableService.findById(PushTask.class, id))
			.switchIfEmpty(Mono.error(new HttpStatusException(
				HttpStatus.NOT_FOUND,
				"PushTask not found with ID: " + id
			)))
			.flatMap(pt -> Mono.from(pushableService.delete(PushTask.class, (PushTask) pt)))
			.then();
	}
}
