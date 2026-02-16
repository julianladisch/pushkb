package com.k_int.pushKb.api.errors;

import io.micronaut.http.HttpMethod;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Standard error response body for the PushKB API.
 * <p>
 * This class provides a consistent structure for all API errors, including
 * contextual information about the request and a list of specific sub-errors
 * (e.g., validation failures).
 */
@Data
@Serdeable
@Builder
@Schema(
	name = "PushkbAPIError",
	description = "A standardized error response containing request metadata and failure details."
)
public class PushkbAPIError {

	/**
	 * The moment in time when the error occurred.
	 */
	@Schema(description = "ISO-8601 timestamp of the error", example = "2026-02-16T13:17:44Z")
	Instant timestamp;

	/**
	 * The full URI of the request that triggered the error.
	 */
	@Schema(description = "The requested URI", example = "/pushtasks/123")
	URI uri;

	/**
	 * The specific field or property path associated with the error (if applicable).
	 */
	@Schema(description = "The property path related to the error")
	String path;

	/**
	 * The HTTP method used for the request.
	 */
	@Schema(description = "The HTTP method of the request")
	HttpMethod method;

	/**
	 * A human-readable summary of the error.
	 */
	@Schema(description = "A summary message of the error", example = "Something went wrong")
	String message;

	/**
	 * A list of nested errors, typically used for multiple validation failures.
	 */
	@Schema(description = "A list of specific sub-errors or validation issues")
	List<PushkbAPIError> errors;
}
