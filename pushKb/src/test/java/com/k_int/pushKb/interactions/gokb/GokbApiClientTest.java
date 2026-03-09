package com.k_int.pushKb.interactions.gokb;

import com.k_int.pushKb.interactions.gokb.model.GokbScrollResponse;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static io.micronaut.http.HttpHeaders.USER_AGENT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// This test is heftily AI generated, please feel free to fix and improve upon it
class GokbApiClientTest {

	private HttpClient mockClient;
	private GokbApiClient gokbClient;

	@BeforeEach
	void setup() {
		mockClient = mock(HttpClient.class);
		gokbClient = new GokbApiClient(mockClient);
	}

	@Test
	@DisplayName("Verify scrollTipps applies correct query parameters and User-Agent")
	void testScrollTipps() {
		// Use the builder as defined in your model
		GokbScrollResponse mockResponse = GokbScrollResponse.builder()
			.scrollId("new-id")
			.records(Collections.emptyList())
			.build();

		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class), any(Argument.class)))
			.thenReturn(Mono.just(HttpResponse.ok(mockResponse)));

		Instant now = Instant.parse("2023-10-01T10:00:00.12345Z");
		var result = gokbClient.scrollTipps("some-scroll-id", now);

		StepVerifier.create(result)
			.expectNext(mockResponse)
			.verifyComplete();

		ArgumentCaptor<MutableHttpRequest<?>> requestCaptor = ArgumentCaptor.forClass(MutableHttpRequest.class);
		verify(mockClient).exchange(requestCaptor.capture(), any(Argument.class), any(Argument.class));

		MutableHttpRequest<?> capturedRequest = requestCaptor.getValue();

		assertEquals(GokbApiClient.GOKB_USER_AGENT, capturedRequest.getHeaders().get(USER_AGENT));

		String uri = capturedRequest.getUri().toString();
		assertTrue(uri.contains("component_type=TitleInstancePackagePlatform"));
		// Verify truncation: seconds only, no millis
		assertTrue(uri.contains("changedSince=2023-10-01T10%3A00%3A00Z"));
	}

	@Test
	@DisplayName("Verify header merging logic in createRequest")
	void testHeaderMerging() {
		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class)))
			.thenReturn(Mono.just(HttpResponse.ok()));

		// Pass a custom header consumer to test the merging branch
		var result = gokbClient.get(
			"/headers",
			String.class,
			Optional.empty(),
			Optional.empty(),
			Optional.of(headers -> headers.add(AUTHORIZATION, "Bearer token"))
		);

		// This block() was triggering the NPE because exchange returned null
		result.block();

		verify(mockClient).exchange(argThat(req ->
			GokbApiClient.GOKB_USER_AGENT.equals(req.getHeaders().get(USER_AGENT)) &&
				"Bearer token".equals(req.getHeaders().get(AUTHORIZATION))
		), any(Argument.class));
	}

	@Test
	@DisplayName("Verify that unsupported methods (DELETE) throw RuntimeException")
	void testUnsupportedMethod() {
		var result = gokbClient.delete("/any", String.class, Optional.empty());

		StepVerifier.create(result)
			.expectErrorMatches(throwable ->
				throwable instanceof RuntimeException &&
					throwable.getMessage().equals("Not supported")
			)
			.verify();

		verifyNoInteractions(mockClient);
	}

	@Test
	@DisplayName("Verify scrollPackages uses correct component type")
	void testScrollPackages() {
		when(mockClient.exchange(any(MutableHttpRequest.class), any(Argument.class), any(Argument.class)))
			.thenReturn(Mono.just(HttpResponse.ok(GokbScrollResponse.builder().build())));

		var result = gokbClient.scrollPackages(null, null);

		StepVerifier.create(result).expectNextCount(1).verifyComplete();

		verify(mockClient).exchange(argThat(req ->
			req.getUri().toString().contains("component_type=Package")
		), any(), any());
	}
}
