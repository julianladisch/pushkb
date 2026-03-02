package com.k_int.pushKb.transform.api;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Tag(
	name="Transforms",
	description="\"Transforms\" represent a way to transform SourceRecords from one shape " +
		"to another. They are used in \"PushTask\" objects to configure the transformations from " +
		"Source to Destination"
)
public interface TransformApi {
	@Operation(
		method="GET",
		summary = "Get Transform Implementers",
		description = "Returns a list of all classes that implement the Transform interface."
	)
	@ApiResponse(
		responseCode = "200",
		description = "Successfully returned all classes implementing the Transform interface"
	)
	Mono<Page<String>> getImplementers(@Valid Pageable pageable);
}
