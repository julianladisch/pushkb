package com.k_int.pushKb.api;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.model.PushTask;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="PushTasks", description="\"PushTasks\" represent the configuration connecting \"Destinations\" and " +
	"\"Sources\" to define what data is pushed where and when. This is purely configuration, the actual scheduling of " +
	"tasks is handled by \"DutyCycleTask\" objects from the Taskscheduler library.")
public interface PushTaskApi {

	@Operation(
		method="POST",
		summary="Create a new PushTask",
		description="Creates a new PushTask in the system. This ensures the PushTask is created, and a DutyCycleTask " +
			"is set up to manage scheduling of the tasks. If a PushTask with the same unique constraints already exists, " +
			"a CONFLICT error is returned instead"
	)
	@Status(HttpStatus.CREATED)
	@ApiResponse(
		responseCode = "201", description = "A PushTask was successfully created, " +
			"and relevant DutyCycleTasks were successfully created."
	)
	@ApiResponse(
		responseCode = "409",
		description = "A PushTask could not be created with those details " +
			"as it conflicts with one already in the system."
	)
	Publisher<PushTask> post(
		@Valid @Body PushTask pt
	);

	@Operation(
		method="DELETE",
		summary = "Delete PushTask",
		description = "Deletes the PushTask with the given id from the system. This also removes the associated " +
			"DutyCycleTask that was managing the scheduling of pushes for this PushTask."
	)
	@Status(HttpStatus.NO_CONTENT)
	@ApiResponse(responseCode = "204", description = "The PushTask was successfully deleted and DutyCycleTask cleaned up.")
	@ApiResponse(
		responseCode = "404",
		description = "Could not find a PushTask with that id.",
		content = @Content(
			schema = @Schema(
				implementation = PushkbAPIError.class
			)
		)
	)
	Publisher<Void> delete(
		@Parameter UUID id
	);

	@Operation(
		method="PUT",
		summary = "Reset PushTask pointers",
		description = "Resets the pointers for the PushTask with the given id. This causes the PushTask to reprocess " +
			"all data from its Source the next time it is run."
	)
	@ApiResponse(responseCode = "404", description = "Could not find a PushTask with that id.", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@ApiResponse(responseCode = "200", description = "Successfully reset pointers for PushTask")
	@SingleResult
	Publisher<PushTask> resetPointers(
		@Parameter UUID id
	);

	@Operation(
		method="PUT",
		summary = "Update PushTask",
		description = "Updates the PushTask with the given id in the system. PUT is not supported on PushTasks - " +
			"POST/DELETE are supported, as well as resetting the pointers."
	)
	@ApiResponse(responseCode = "405", description = "PUT is not allowed for PushTasks")
	Publisher<PushTask> put(
		@Parameter UUID id,
		@Valid @Body PushTask pt
	);
}
