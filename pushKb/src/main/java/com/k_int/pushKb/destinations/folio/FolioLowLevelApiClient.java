package com.k_int.pushKb.destinations.folio;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import javax.print.attribute.standard.Destination;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Singleton
@Client(id = "FOLIO")
public class FolioLowLevelApiClient {
	private final HttpClient httpClient;
	private final URI uri;

	// Logging
	static final Logger log = LoggerFactory.getLogger(FolioLowLevelApiClient.class);

	public FolioLowLevelApiClient(
		HttpClient httpClient,
		Destination destination
	) {  
			this.httpClient = httpClient;
			uri = UriBuilder.of(destination.getURI()) // FIXME Is this dynamic?
							.path("erm")
							.path("pushKB")
							.queryParam("stats", true) // Default to include stats
							.build();
	}

	@SingleResult
	@Retryable
	public Publisher<String> getChunks() {
		URI chunksUri = UriBuilder.of(uri)
															.path("/chunks")
															.build();

		HttpRequest<?> req = HttpRequest.GET(chunksUri)
			.header(USER_AGENT, "PushKB FOLIO Client")
			.header(ACCEPT, "application/json")
			.header("X-Okapi-Tenant", "test1"); // FIXME this isn't dynamic??
		return httpClient.retrieve(req);
	}
}