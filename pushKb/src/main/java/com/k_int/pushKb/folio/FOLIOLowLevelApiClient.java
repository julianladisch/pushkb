package com.k_int.pushKb.folio;

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
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Singleton
public class FOLIOLowLevelApiClient {
	private final HttpClient httpClient;
	private final URI uri;

	// Logging
	static final Logger log = LoggerFactory.getLogger(FOLIOLowLevelApiClient.class);

	public FOLIOLowLevelApiClient(
		@Client(id = "FOLIO") HttpClient httpClient,
		String baseUrl
	) {  
			this.httpClient = httpClient;
			uri = UriBuilder.of(baseUrl) // FIXME Is this dynamic?
							.path("erm")
							.path("pushKB")
							.build();
	}

	@Get("/chunks")
	@SingleResult
	@Retryable
	public Publisher<String> getChunks() {
		log.info("WHAT IS URI?: {}", uri);
		// FIXME this casting to string sucks
		HttpRequest<?> req = HttpRequest.GET(uri.toString() + "/chunks")
			.header(USER_AGENT, "Micronaut HTTP Client TEST")
			.header(ACCEPT, "application/json")
			.header("X-Okapi-Tenant", "test1"); // FIXME this isn't dynamic??
		return httpClient.retrieve(req);
	}
}

/* 
@Client("${" + GokbApiClient.CONFIG_ROOT + ".api.url:`https://gokb.org/gokb/api`}")
@Header(name = ACCEPT, value = APPLICATION_JSON)
public interface GokbApiClient {
	public static final String COMPONENT_TYPE_TIPP = "TitleInstancePackagePlatform";
	public static final String COMPONENT_TYPE_PACKAGE = "Package";

	public static final String QUERY_PARAM_COMPONENT_TYPE = "component_type";

	public final static String CONFIG_ROOT = "gokb.client";
	static final Logger log = LoggerFactory.getLogger(GokbApiClient.class);

	@SingleResult
	public default Publisher<GokbScrollResponse> scrollTipps(@Nullable String scrollId, @Nullable Instant changedSince) {
		return scroll(COMPONENT_TYPE_TIPP, scrollId, changedSince != null ? changedSince.truncatedTo(ChronoUnit.SECONDS).toString() : null);
	}

	@SingleResult
	public default Publisher<GokbScrollResponse> scrollPackages(@Nullable String scrollId, @Nullable Instant changedSince) {
		return scroll(COMPONENT_TYPE_PACKAGE, scrollId, changedSince != null ? changedSince.truncatedTo(ChronoUnit.SECONDS).toString() : null);
	}

	@Get("/scroll")
	@SingleResult
	@Retryable
	@Header(name = "user-agent", value = "pushKB")
	abstract <T> Publisher<GokbScrollResponse> scroll(
		@NonNull @NotBlank @QueryValue(GokbApiClient.QUERY_PARAM_COMPONENT_TYPE) String type,
		@Nullable @QueryValue("scrollId") String scrollId,
		@Nullable @QueryValue("changedSince") String changedSince
	);
}
 */