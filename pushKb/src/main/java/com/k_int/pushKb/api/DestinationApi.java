package com.k_int.pushKb.api;

import com.k_int.pushKb.model.DestinationImplementersDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@Tag(name="Destinations", description="\"Destinations\" represent the push side of PushKB, places to which data from \"Sources\" can be sent.")
public interface DestinationApi {
	@Operation(
		method="GET",
		summary = "Get Destination Implementers",
		description = "Returns a list of all classes that implement the Destination interface."
	)
	@ApiResponse(
		responseCode = "200",
		description = "Successfully returned all classes implementing the Destination interface"
	)
	Mono<DestinationImplementersDTO> getImplementers();
}
