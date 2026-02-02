package com.k_int.pushKb.api;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Tag(name="SourceRecords", description="SourceRecords represent individual records obtained from Sources, stored in a " +
	"queue ready to be transformed and pushed to Destinations configured via PushTasks.")
public interface SourceRecordApi {
	@Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
	@Operation(
		method="GET",
		summary = "Count SourceRecords",
		description = "Returns the total number of SourceRecords available, optionally filtered by sourceId " +
			"and/or filterContext."
	)
	public Publisher<Long> count(
		@Nullable @QueryValue UUID sourceId,
		@Nullable @QueryValue String filterContext
	);

	@Operation(
		method="DELETE",
		summary = "Clear all SourceRecords",
		description = "Deletes all SourceRecords from the system. WARNING: This operation is irreversible and will remove " +
			"all records, requiring a new source harvest for ALL Source implementations. It also currently does NOT reset the " +
			"pointers for sources, so user beware."
	)
	@Delete(uri = "/clearRecords", produces = MediaType.APPLICATION_JSON)
	public Publisher<Long> clearRecords();
}
