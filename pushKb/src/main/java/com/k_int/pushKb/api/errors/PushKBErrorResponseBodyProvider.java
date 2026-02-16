package com.k_int.pushKb.api.errors;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.JsonErrorResponseBodyProvider;
import io.micronaut.json.JsonConfiguration;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Replaces the DefaultJsonErrorResponseBodyProvider
@Singleton
public class PushKBErrorResponseBodyProvider implements JsonErrorResponseBodyProvider<PushkbAPIError> {
	private final boolean alwaysSerializeErrorsAsList;

	PushKBErrorResponseBodyProvider(JsonConfiguration jacksonConfiguration) {
		this.alwaysSerializeErrorsAsList = jacksonConfiguration.isAlwaysSerializeErrorsAsList();
	}

	@Override
	public PushkbAPIError body(ErrorContext errorContext, HttpResponse<?> response) {
		PushkbAPIError apiError = PushkbAPIError.builder()
			.timestamp(Instant.now())
			.uri(errorContext.getRequest().getUri())
			.method(errorContext.getRequest().getMethod())
			.build();

		if (!errorContext.hasErrors()) {
			apiError.setMessage(response.reason());
		} else if (errorContext.getErrors().size() == 1 && !alwaysSerializeErrorsAsList) {
			Error jsonError = errorContext.getErrors().get(0);
			apiError.setMessage(jsonError.getMessage());
			jsonError.getPath().ifPresent(apiError::setPath);
		} else {
			apiError.setMessage(response.reason());
			List<PushkbAPIError> errors = new ArrayList<>(errorContext.getErrors().size());
			for (Error jsonError : errorContext.getErrors()) {
				errors.add(
					PushkbAPIError
						.builder()
						.message(jsonError.getMessage())
						.path(jsonError.getPath().orElse(null))
						.build()
				);
			}
			apiError.setErrors(errors);
		}

		return apiError;
	}
}
