package com.k_int.pushKb.api;

import com.k_int.pushKb.model.responses.TaskResetDTO;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST Controller for managing {@link DutyCycleTask} instances.
 * <p>
 * Provides endpoints for listing tasks and performing administrative actions
 * like manually resetting task statuses within the scheduler.
 */
// TODO this perhaps ought to be an offering from the taskscheduler library itself
@Controller("/dutycycletasks")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class DutyCycleTaskController implements DutyCycleTaskApi {
	private final ReactiveDutyCycleTaskRepository dutyCycleTaskRepository;
	private final ReactiveDutyCycleTaskRunner runner;

	/**
	 * Constructs the controller with necessary scheduler services.
	 *
	 * @param dutyCycleTaskRepository The repository for DCT persistence.
	 * @param runner The runner service responsible for task lifecycle and execution.
	 */
	public DutyCycleTaskController(
		ReactiveDutyCycleTaskRepository dutyCycleTaskRepository,
		ReactiveDutyCycleTaskRunner runner
	) {
		this.dutyCycleTaskRepository = dutyCycleTaskRepository;
		this.runner = runner;
	}

	/**
	 * Retrieves all Duty Cycle Tasks currently registered in the system.
	 *
	 * @return A {@link Mono} emitting a {@link Page} of {@link DutyCycleTask} objects.
	 */
	@Get(produces = MediaType.APPLICATION_JSON)
	public Mono<Page<DutyCycleTask>> getDutyCycleTasks(@Valid Pageable pageable) {
		return Mono.from(dutyCycleTaskRepository.findAll(pageable));
	}

	/**
	 * Resets a task status to IDLE.
	 * <p>
	 * <b>WARNING:</b> Manually resetting a task status can cause significant issues if a task
	 * is actually running for that DutyCycleTask (DCT) on any node. This can result
	 * in duplicate execution, race conditions, or data corruption. Use this only
	 * to recover "stuck" tasks that are confirmed to be inactive.
	 *
	 * @param id The UUID of the Duty Cycle Task to reset.
	 * @return A {@link Mono} emitting a {@link TaskResetDTO} confirming the reset.
	 */
	@Post(value = "/{id}/reset", produces = MediaType.APPLICATION_JSON)
	public Mono<TaskResetDTO> resetTask(@NonNull UUID id) {
		log.info("Request to manually reset DutyCycleTask: {}", id);

		return Mono.from(runner.manualReset(id))
			.thenReturn(
				TaskResetDTO
					.builder()
					.status("success")
					.id(id)
					.message("Task status has been reset to IDLE")
					.build()
				);
	}
}
