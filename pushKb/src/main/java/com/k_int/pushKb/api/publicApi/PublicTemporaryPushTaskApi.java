package com.k_int.pushKb.api.publicApi;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
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
	public Mono<MutableHttpResponse<Map<String, Object>>> temporaryPushTask(
		String pushTaskId,
		@Nullable String filterContext
	);
}
