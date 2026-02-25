package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="Sources: GokbSource", description="A GokbSource is an implementation of the \"Source\" interface that " +
	"represents a GOKB instance from which data can be pulled into PushKB, separated into granular sources for TIPP " +
	"vs Package vs Provider, etc. It contains shared configuration information about the instance itself in the " +
	"Gokb object.")
public interface GokbSourceApi {

	@Operation(
		method="POST",
		summary="Create a new GokbSource",
		description="Creates a new GokbSource. Enforces uniqueness based on the Gokb instance and SourceType. " +
			"**Note:** If the nested 'Gokb' object provided in the request does not exist, it will be created. " +
			"If it already exists, the source will be associated with the existing instance. " +
			"If a conflict occurs at the Source level, 409 is returned. Use PUT for updates."
	)
	@Status(HttpStatus.CREATED)
	@ApiResponse(responseCode = "201", description = "Created")
	@ApiResponse(responseCode = "409", description = "Conflict - Identity already exists")
	Publisher<GokbSource> post(@Valid @Body GokbSource src);

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
