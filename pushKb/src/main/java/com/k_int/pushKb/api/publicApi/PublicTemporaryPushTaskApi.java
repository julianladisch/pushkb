package com.k_int.pushKb.api.publicApi;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import com.k_int.pushKb.model.TemporaryPushTask;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name="PushTasks: Public", description="APIs for managing PushTasks that are exposed publicly and are NOT " +
	"authenticated."
)
public interface PublicTemporaryPushTaskApi {
	// TODO add @Body binding shape
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	@Operation(
		method="POST",
		summary="Create a new TemporaryPushTask",
		description="Creates a new TemporaryPushTask in the system. " +
			"A TemporaryPushTask is a one-off PushTask that is not persisted in the system, " +
			"but can be used to push data to a Destination from a Source without creating a permanent PushTask. " +
			"This is useful for testing or ad-hoc pushes."
	)
	@ApiResponse(responseCode = "200", description = "Successfully created TemporaryPushTask")
	@ApiResponse(responseCode = "409", description = "Unable to create TemporaryPushTask as one already exists", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@ApiResponse(responseCode = "404", description = "Unable to create TemporaryPushTask as PushTask doesn't exist with that id", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))

	public Mono<TemporaryPushTask> temporaryPushTask(
		String pushTaskId,
		@Nullable String filterContext
	);
}
