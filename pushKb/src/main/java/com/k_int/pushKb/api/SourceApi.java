package com.k_int.pushKb.api;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Tag(name="Sources", description="\"Sources\" represent the pull side of PushKB, places from which data can be obtained to be sent to \"Destinations\".")
public interface SourceApi {
	@Operation(
		method="GET",
		summary = "Get Source Implementers",
		description = "Returns a list of all classes that implement the Source interface."
	)
	@ApiResponse(
		responseCode = "200",
		description = "Successfully returned all classes implementing the Source interface"
	)
	Mono<Page<String>> getImplementers(@Valid Pageable pageable);
}
