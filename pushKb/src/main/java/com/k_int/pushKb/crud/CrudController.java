package com.k_int.pushKb.crud;

import java.util.UUID;

import com.k_int.pushKb.api.errors.PushkbAPIError;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.Body;
import jakarta.validation.Valid;

public interface CrudController<T extends HasId> {

	@Operation(
		method="POST",
		summary = "Create entry",
		description = "Creates a new record in the system. Because this resource uses deterministic IDs, " +
			"if a record with the same identity properties already exists, a 409 Conflict will be returned. " +
			"To modify an existing record, use the PUT endpoint."
	)
	@Status(HttpStatus.CREATED)
	@ApiResponse(responseCode = "201", description = "Record created")
	@ApiResponse(responseCode = "409", description = "Conflict - Record already exists", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@SingleResult
	Publisher<T> post(@Valid @Body T obj);

	@Operation(
		method="GET",
	  summary = "List entries",
		description = "Returns a page response containing a list of records from the system."
	)
	@ApiResponse(responseCode = "200", description = "A page of resources")
  Publisher<Page<T>> list(@Valid Pageable pageable);

	@Operation(
		method="GET",
		summary = "Get entry by ID",
		description = "Returns the record with the given id from the system."
	)
	@ApiResponse(responseCode = "200", description = "Success")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@SingleResult
  Publisher<T> get(@Parameter UUID id);

	@Operation(
		method="PUT",
		summary = "Update entry",
		description = "Updates the record with the given id in the system."
	)
	@ApiResponse(responseCode = "200", description = "Updated")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@ApiResponse(responseCode = "400", description = "Bad request -- Immutable identifier changed, fields missing or incorrect JSON.", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@SingleResult
  Publisher<T> put(@Parameter UUID id, @Valid @Body T obj);

	@Operation(
		method="DELETE",
		summary = "Delete entry",
		description = "Deletes the record with the given id from the system."
	)
	@ApiResponse(responseCode = "204", description = "Deleted")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = PushkbAPIError.class)))
	@SingleResult
  Publisher<Void> delete(@Parameter UUID id);

	@Operation(
		method="GET",
		summary = "Count entries",
		description = "Returns the total number of records available for this resource type."
	)
	@ApiResponse(responseCode = "200", description = "The number of records in the database as a Long", content = @Content(schema = @Schema(implementation = Long.class)))
	@SingleResult
	Publisher<Long> count();
}
