package com.k_int.pushKb.api;

import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.Map;

// TODO this perhaps ought to be an offering from the taskscheduler library itself
@Controller("/dutycycletasks")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class DutyCycleTaskController {
	private final ReactiveDutyCycleTaskRepository dutyCycleTaskRepository;
	private final ReactiveDutyCycleTaskRunner runner;

	public DutyCycleTaskController(
		ReactiveDutyCycleTaskRepository dutyCycleTaskRepository,
		ReactiveDutyCycleTaskRunner runner
	) {
		this.dutyCycleTaskRepository = dutyCycleTaskRepository;
		this.runner = runner;
	}

	@Get(produces = MediaType.APPLICATION_JSON)
	public Publisher<DutyCycleTask> getDutyCycleTasks() {
		return Flux.from(dutyCycleTaskRepository.findAll());
	}

	/**
	 * Resets a task status to IDLE.
	 * WARNING: Manually resetting a task status can cause significant issues if a task
	 * is actually running for that DutyCycleTask (DCT) on any node. This can result
	 * in duplicate execution or data corruption.
	 */
	@Post(value = "/{id}/reset", produces = MediaType.APPLICATION_JSON)
	@Operation(
		summary = "Manually reset a task to IDLE",
		description = "DANGER: Resets a 'stuck' task to IDLE and clears local runner references. " +
			"This can cause duplicate execution if the task is still active on another node. " +
			"Use only when a task is confirmed to be stalled due to a node crash."
	)
	@ApiResponse(responseCode = "200", description = "Task was successfully reset.")
	@ApiResponse(responseCode = "404", description = "Task ID not found.")
	public Publisher<Map<String,String>> resetTask(@NonNull UUID id) {
		log.info("Request to manually reset DutyCycleTask: {}", id);

		return Mono.from(runner.manualReset(id)).thenReturn(Map.of(
			"status", "success",
			"id", id.toString(),
			"message", "Task status has been reset to IDLE"
		));
	}
}
