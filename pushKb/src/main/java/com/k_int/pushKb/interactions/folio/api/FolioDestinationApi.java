package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

@Tag(name="Destinations: FolioDestination", description="A FolioDestination is an implementation of \"Destination\" " +
	"interface that represents a FOLIO instance to which data can be pushed along the \"/erm/pushKB/*\" " +
	"endpoints. It contains configuration about the FOLIO tenant to which data will be pushed as a FolioTenant.")
public interface FolioDestinationApi {

	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	@Operation(
		method="POST",
		summary="Create a new FolioDestination",
		description="Creates a new FolioDestination in the system." +
			"If a FolioDestination with the same unique constraints already exists," +
			"it will be returned instead of creating a duplicate. " +
			"FolioTenant can be created via this call as well, or used if it already exists."
	)
	public Publisher<FolioDestination> post(
		@Valid @Body FolioDestination dest
	);
}
