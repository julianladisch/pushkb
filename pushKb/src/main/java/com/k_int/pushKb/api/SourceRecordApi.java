package com.k_int.pushKb.api;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="SourceRecords", description="SourceRecords represent individual records obtained from Sources, stored in a " +
	"queue ready to be transformed and pushed to Destinations configured via PushTasks.")
public interface SourceRecordApi {
	@Operation(
		method="GET",
		summary = "Count SourceRecords",
		description = "Returns the total number of SourceRecords available, optionally filtered by sourceId " +
			"and/or filterContext."
	)
	@ApiResponse(responseCode = "200", description = "Successfully collect the number of SourceRecord objects in the database (optionally filtered)")
	@SingleResult
	Publisher<Long> count(
		@Nullable @QueryValue UUID sourceId,
		@Nullable @QueryValue String filterContext
	);

	@SingleResult
	@Operation(
		method="DELETE",
		summary = "Clear all SourceRecords",
		description = "Deletes all SourceRecords from the system. WARNING: This operation is irreversible and will remove " +
			"all records, requiring a new source harvest for ALL Source implementations. It also currently does NOT reset the " +
			"pointers for sources, so user beware."
	)
	@ApiResponse(responseCode = "204", description = "All SourceRecord objects have been successfully cleared from the database")
	Publisher<Void> clearRecords();
}
