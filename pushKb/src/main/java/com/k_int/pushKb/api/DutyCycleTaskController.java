package com.k_int.pushKb.api;

import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

// TODO this perhaps ought to be an offering from the taskscheduler library itself
@Controller("/dutycycletasks")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class DutyCycleTaskController {
	private final ReactiveDutyCycleTaskRepository dutyCycleTaskRepository;
	public DutyCycleTaskController(
		ReactiveDutyCycleTaskRepository dutyCycleTaskRepository
	) {
		this.dutyCycleTaskRepository = dutyCycleTaskRepository;
	}

	@Get(produces = MediaType.APPLICATION_JSON)
	public Publisher<DutyCycleTask> getDutyCycleTasks() {
		return Flux.from(dutyCycleTaskRepository.findAll());
	}
}
