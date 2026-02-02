package com.k_int.pushKb.api;

import com.k_int.pushKb.model.PushTask;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
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
		description="Creates a new PushTask in the system. If a PushTask with the same unique constraints already exists, " +
			"it will be returned instead of creating a duplicate. This ensures the PushTask is created, and a DutyCycleTask " +
			"is set up to manage scheduling of the tasks"
	)
	@Post(produces = MediaType.APPLICATION_JSON)
	Publisher<PushTask> post(
		@Valid @Body PushTask pt
	);

	@Operation(
		method="DELETE",
		summary = "Delete PushTask",
		description = "Deletes the PushTask with the given id from the system. This also removes the associated " +
			"DutyCycleTask that was managing the scheduling of pushes for this PushTask."
	)
	@Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Publisher<Long> delete(
		@Parameter UUID id
	);

	@Operation(
		method="PUT",
		summary = "Reset PushTask pointers",
		description = "Resets the pointers for the PushTask with the given id. This causes the PushTask to reprocess " +
			"all data from its Source the next time it is run."
	)
	@Put(uri = "/{id}/resetPointers", produces = MediaType.APPLICATION_JSON)
	public Publisher<PushTask> resetPointers(
		@Parameter UUID id
	);

	@Put(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	@Hidden // This isn't supported so hide it from the docs
	@Operation(
		method="PUT",
		summary = "Update PushTask",
		description = "Updates the PushTask with the given id in the system. PUT is not supported on PushTasks - " +
			"POST/DELETE are supported, as well as resetting the pointers."
	)
	public Publisher<PushTask> put(
		@Parameter UUID id,
		@Valid @Body PushTask pt
	);
}
