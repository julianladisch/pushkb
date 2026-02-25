package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

@Tag(name="Destinations: FolioDestination", description="A FolioDestination is an implementation of \"Destination\" " +
	"interface that represents a FOLIO instance to which data can be pushed along the \"/erm/pushKB/*\" " +
	"endpoints. It contains configuration about the FOLIO tenant to which data will be pushed as a FolioTenant.")
public interface FolioDestinationApi {

	@Operation(
		method="POST",
		summary="Create a new FolioDestination",
		description="Creates a new FolioDestination record. IDs are generated deterministically " +
			"from the FolioTenant and DestinationType. " +
			"**Nested Creation:** The provided 'FolioTenant' configuration will be created if it does not " +
			"already exist (including Vault secret initialization). If the tenant exists, it will be linked. " +
			"If this specific Destination already exists, a 409 Conflict is returned. To update, use the PUT endpoint."
	)
	@Status(HttpStatus.CREATED)
	@ApiResponse(responseCode = "201", description = "FolioDestination created successfully")
	Publisher<FolioDestination> post(
		@Valid @Body FolioDestination dest
	);
}
