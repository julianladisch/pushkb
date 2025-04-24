package com.k_int.pushKb.api;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.storage.PushTaskRepository;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/pushtasks")
public class PushTaskController extends CrudControllerImpl<PushTask> {
  private final PushTaskRepository repository;
  // FIXME this MUST handle registering of pushTasks and deregistering from taskscheduler
  public PushTaskController(PushTaskRepository repository) {
    super(repository);

    this.repository = repository;
  }

  @Override
  @Post(produces = MediaType.APPLICATION_JSON)
  public Publisher<PushTask> post(
		@Valid @Body PushTask pt
	) {
		pt.setId(repository.generateUUIDFromObject(pt));

		// Bit clunky, but ensure we have pointers either sent down or reset
		if (
			pt.getDestinationHeadPointer() == null &&
			pt.getLastSentPointer() == null &&
			pt.getFootPointer() == null
		)	{
			pt.resetPointer();
		}

		// TODO This should have existById protection, as well as cleaning up these defaults etc etc.

    return repository.save(pt);
  }

  // FIXME It ALSO should not allow completely basic POST functionality... we need to build in existsById protection
  // (and implementing class protection?)


  // TODO reset pointer endpoint?
}
