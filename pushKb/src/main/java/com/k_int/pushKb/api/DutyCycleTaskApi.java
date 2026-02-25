package com.k_int.pushKb.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.model.responses.TaskResetDTO;
import com.k_int.taskscheduler.model.DutyCycleTask;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="DutyCycleTasks", description="Endpoints for managing DutyCycleTasks in the Taskscheduler. " +
	"These objects are responsible for handling the actual scheduling of system operations, such as Source pulls " +
	"and PushTask pushes to Destinations. This is NOT the location of the configuration of Source -> Destination, " +
	"that is PushTask. These objects are created implicitly alongside the management of PushTasks"
)
public interface DutyCycleTaskApi {
	@Operation(
		summary = "Get all DutyCycleTasks",
		description = "Returns a list of all DutyCycleTasks in the system. " +
			"These objects represent the scheduled tasks that are run to perform operations such as pulling from Sources " +
			"and pushing to Destinations based on the configuration defined in PushTasks."
	)
	Publisher<Page<DutyCycleTask>> getDutyCycleTasks(@Valid Pageable pageable);

	/**
	 * Resets a task status to IDLE.
	 * WARNING: Manually resetting a task status can cause significant issues if a task
	 * is actually running for that DutyCycleTask (DCT) on any node. This can result
	 * in duplicate execution or data corruption.
	 */
	@Operation(
		summary = "Manually reset a task to IDLE",
		description = "DANGER: Resets a 'stuck' task to IDLE and clears local runner references. " +
			"This can cause duplicate execution if the task is still active on another node. " +
			"Use only when a task is confirmed to be stalled due to a node crash."
	)
	@ApiResponse(responseCode = "200", description = "Task was successfully reset.")
	@ApiResponse(responseCode = "404", description = "Task ID not found.", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@SingleResult
	Publisher<TaskResetDTO> resetTask(@NonNull UUID id);
}
