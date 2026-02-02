package com.k_int.pushKb.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name="Destinations", description="\"Destinations\" represent the push side of PushKB, places to which data from \"Sources\" can be sent.")
public interface DestinationApi {
	@Operation(
		method="GET",
		summary = "Get Destination Implementors",
		description = "Returns a list of all classes that implement the Destination interface."
	)
	Mono<Map<String, Object>> getImplementors();
}
