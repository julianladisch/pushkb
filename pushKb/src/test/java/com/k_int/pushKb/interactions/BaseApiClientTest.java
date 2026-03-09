package com.k_int.pushKb.interactions;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BaseApiClientTest {

	private HttpClient mockClient;
	private BaseApiClient apiClient;

	@BeforeEach
	void setup() {
		mockClient = mock(HttpClient.class);
		// BaseApiClient is usually abstract or used as a base,
		// but we can instantiate it directly for logic testing.
		apiClient = new BaseApiClient(mockClient);
	}

	@Test
	@DisplayName("Test building a request")
	void testGetRequestBuilding() {
		String path = "/test-api";
		HttpResponse<String> mockResponse = HttpResponse.ok("Success");

		// Mock the exchange call
		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class)))
			.thenReturn(Mono.just(mockResponse));

		Mono<HttpResponse<String>> result = apiClient.get(
			path,
			String.class,
			Optional.empty(),
			Optional.of(uri -> uri.queryParam("foo", "bar")), // UriBuilderConsumer
			Optional.of(headers -> headers.add("X-Test", "Value")) // HeaderConsumer
		);

		StepVerifier.create(result)
			.assertNext(response -> {
				assertEquals(HttpStatus.OK, response.getStatus());
				assertEquals("Success", response.body());
			})
			.verifyComplete();

		// Verify that headers/params were applied to the request passed to the client
		verify(mockClient).exchange(argThat(req ->
			req.getPath().equals(path) &&
				req.getUri().getQuery().contains("foo=bar") &&
				req.getHeaders().get("X-Test").equals("Value")
		), any(Argument.class));
	}

	@Test
	@DisplayName("Test error mapping")
	void testErrorMapping() {
		// 1. Setup mock exception
		HttpClientResponseException innerException = mock(HttpClientResponseException.class);
		when(innerException.getStatus()).thenReturn(HttpStatus.BAD_REQUEST);
		// Important: Micronaut exceptions often expect a non-null response
		when(innerException.getResponse()).thenReturn(HttpResponse.badRequest());

		// 2. Mock the client to return the error
		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class)))
			.thenReturn(Mono.error(innerException));

		// 3. Trigger the call
		Mono<HttpResponse<String>> result = apiClient.get("/error", String.class, Optional.empty());

		// 4. Use satisfies for better error messages
		StepVerifier.create(result)
			.expectErrorSatisfies(throwable -> {
				// Check the class type
				assertEquals(HttpClientRequestResponseException.class, throwable.getClass(),
					"The exception should be wrapped in our custom HttpClientRequestResponseException");

				// Check the cause
				// We check if the cause is equal to our mock innerException
				assertEquals(innerException, throwable.getCause(),
					"The original HttpClientResponseException should be the cause");
			})
			.verify();
	}

	@Test
	@DisplayName("Test sending a POST with a body")
	void testPostWithBody() {
		Map<String, String> body = Map.of("key", "value");
		HttpResponse<String> mockResponse = HttpResponse.created("Done");

		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class)))
			.thenReturn(Mono.just(mockResponse));

		Mono<HttpResponse<String>> result = apiClient.post("/create", String.class, Optional.of(body), Optional.empty());

		StepVerifier.create(result)
			.expectNextCount(1)
			.verifyComplete();

		// Verify body was attached
		verify(mockClient).exchange(argThat(req ->
			req.getBody().isPresent() && req.getBody().get().equals(body)
		), any(Argument.class));
	}
}
