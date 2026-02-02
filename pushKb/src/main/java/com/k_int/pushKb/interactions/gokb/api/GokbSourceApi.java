package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="Sources: GokbSource", description="A GokbSource is an implementation of the \"Source\" interface that " +
	"represents a GOKB instance from which data can be pulled into PushKB, separated into granular sources for TIPP " +
	"vs Package vs Provider, etc. It contains shared configuration information about the instance itself in the " +
	"Gokb object.")
public interface GokbSourceApi {

	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	@Operation(
		method="POST",
		summary="Create a new GokbSource",
		description="Creates a new GokbSource in the system." +
			"If a GokbSource with the same unique constraints already exists," +
			"it will be returned instead of creating a duplicate. " +
			"Gokb can be created via this call as well, or used if it already exists."
	)
	Publisher<GokbSource> post(
		@Valid @Body GokbSource src
	);

	@Put(uri = "/{id}/resetPointer", produces = MediaType.APPLICATION_JSON)
	@Operation(
		method="PUT",
		summary="Reset GokbSource pointer",
		description="Resets the internal pointer for the GokbSource with the given id." +
			"This will cause the next data pull to start from the beginning."
	)
	Publisher<GokbSource> resetPointer(
		@Parameter UUID id
	);
}
