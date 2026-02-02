package com.k_int.pushKb.crud;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.Body;
import jakarta.validation.Valid;

public interface CrudController<T extends HasId> {

	@Operation(
		method="POST",
		summary = "Create entry",
		description = "Creates a new record in the system."
	)
  Publisher<T> post(@Valid @Body T obj);


	@Operation(
		method="GET",
	  summary = "List entries",
		description = "Returns a paginated list of records from the system."
	)
  Publisher<List<T>> list(@Valid Pageable pageable);

	@Operation(
		method="GET",
		summary = "Get entry by ID",
		description = "Returns the record with the given id from the system."
	)
  Publisher<T> get(@Parameter UUID id);

	@Operation(
		method="PUT",
		summary = "Update entry",
		description = "Updates the record with the given id in the system."
	)
  Publisher<T> put(@Parameter UUID id, @Valid @Body T obj);

	@Operation(
		method="DELETE",
		summary = "Delete entry",
		description = "Deletes the record with the given id from the system."
	)
  Publisher<Long> delete(@Parameter UUID id);

	@Operation(
		method="GET",
		summary = "Count entries",
		description = "Returns the total number of records available for this resource type."
	)
	Publisher<Long> count();
}
