package com.k_int.pushKb.api;

import com.k_int.pushKb.services.PushTaskDatabaseService;
import com.k_int.pushKb.services.PushableService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.model.PushTask;

import io.micronaut.http.MediaType;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/pushtasks")
public class PushTaskController extends CrudControllerImpl<PushTask> {
	private final PushTaskDatabaseService databaseService;
	private final PushableService pushableService;
  // FIXME this MUST handle registering of pushTasks and deregistering from taskscheduler
  public PushTaskController(
		PushTaskDatabaseService databaseService,
		PushableService pushableService
	) {
    super(databaseService);

    this.databaseService = databaseService;
		this.pushableService = pushableService;
  }

  @Override
  @Post(produces = MediaType.APPLICATION_JSON)
  public Publisher<PushTask> post(
		@Valid @Body PushTask pt
	) {
		pt.setId(databaseService.generateUUIDFromObject(pt));

		return Mono.from(pushableService.ensurePushable(pt)).map(p -> (PushTask) p);
	}

	@Override
	@Put(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Publisher<PushTask> put(
		@Parameter UUID id,
		@Valid @Body PushTask pt
	) {
		return Mono.error(new RuntimeException("PUT is not supported on PushTasks - POST/DELETE are supported, as well as resetting the pointers"));
	}

  // Reset pointer endpoint
	@Put(uri = "/{id}/resetPointers", produces = MediaType.APPLICATION_JSON)
	public Publisher<PushTask> resetPointers(
		@Parameter UUID id
	) {

		return Mono.from(databaseService.findById(id))
			.flatMap(pt -> {
				pt.resetPointer();
				return Mono.from(databaseService.update(pt));
			})
			.switchIfEmpty(Mono.error(new RuntimeException("No PushTask found with id: " + id)));
	}
}
