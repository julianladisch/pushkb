package com.k_int.pushKb.transform.api;

import com.k_int.pushKb.transform.model.ProteusTransform;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Body;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="Transforms: ProteusTransform", description="A ProteusTransform is an implementation of \"Transform\" " +
	"interface that represents a transformation schema from JSON -> JSON using Proteus, an open source extension of " +
	"the JSONPath standard.")
public interface ProteusTransformApi {
	@Operation(
		method="POST",
		summary = "Create ProteusTransform",
		description = "Creates a ProteusTransform spec. POST is not currently supported on ProteusTransform - " +
			"only GET is supported."
	)
	@ApiResponse(responseCode = "405", description = "POST is not allowed for PushTasks")
	Publisher<ProteusTransform> post(
		@Valid @Body ProteusTransform pt
	);

	@Operation(
		method="PUT",
		summary = "Update ProteusTransform",
		description = "Update a ProteusTransform spec. PUT is not currently supported on ProteusTransform - " +
			"only GET is supported."
	)
	@ApiResponse(responseCode = "405", description = "PUT is not allowed for PushTasks")
	Publisher<ProteusTransform> put(
		@Parameter UUID id,
		@Valid @Body ProteusTransform pt
	);

	@Operation(
		method="DELETE",
		summary = "Delete ProteusTransform",
		description = "Delete a ProteusTransform spec. DELETE is not currently supported on ProteusTransform - " +
			"only GET is supported."
	)
	@ApiResponse(responseCode = "405", description = "DELETE is not allowed for PushTasks")
	Publisher<Void> delete(
		@Parameter UUID id
	);
}
