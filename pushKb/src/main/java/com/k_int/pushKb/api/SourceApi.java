package com.k_int.pushKb.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name="Sources", description="\"Sources\" represent the pull side of PushKB, places from which data can be obtained to be sent to \"Destinations\".")
public interface SourceApi {
	@Operation(
		method="GET",
		summary = "Get Source Implementors",
		description = "Returns a list of all classes that implement the Source interface."
	)
	Mono<Map<String, Object>> getImplementors();
}
