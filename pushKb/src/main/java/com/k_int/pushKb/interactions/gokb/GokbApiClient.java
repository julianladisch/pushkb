package com.k_int.pushKb.interactions.gokb;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

import static io.micronaut.http.HttpMethod.GET;
import static io.micronaut.http.HttpMethod.POST;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.BaseApiClient;
import com.k_int.pushKb.interactions.gokb.model.GokbScrollResponse;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.uri.UriBuilder;

import io.micronaut.retry.annotation.Retryable;

// We have one of these _per source_, so we can store the scroll component type on the ApiClient and do that automatically on scroll call
@Slf4j
public class GokbApiClient extends BaseApiClient {
	public final static String USER_AGENT = "pushKB";

	public static final String COMPONENT_TYPE_TIPP = "TitleInstancePackagePlatform";
	public static final String COMPONENT_TYPE_PACKAGE = "Package";

	public static final String SCROLL_URL = "/gokb/api/scroll";
	public static final String SCROLL_ID_QUERY_PARAM = "scrollId";
	public static final String CHANGED_SINCE_QUERY_PARAM = "changedSince";

	public static final String QUERY_PARAM_COMPONENT_TYPE = "component_type";

	public static final List<HttpMethod> acceptedMethods = Arrays.asList(GET);


	public GokbApiClient(
		HttpClient client
	) {
		super(client);
	}

	// This client only supports GET at the moment, so ignore all POST/PUT/DELETE calls
	// Also include userAgent as standard
	@Override
	protected Mono<MutableHttpRequest<?>> createRequest(
    HttpMethod method,
    String path,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {
		if (acceptedMethods.contains(method)) {
			return super.createRequest(
				method,
				path,
				uriBuilderConsumer,
				Optional.of(headers -> {
					headers.set("USER_AGENT", GokbApiClient.USER_AGENT);
					if (httpHeaderConsumer.isPresent()) {
						httpHeaderConsumer.get().accept(headers);
					}
				})
			);
		}

		return Mono.error(new RuntimeException("Not supported"));
	}

	@SingleResult
	@Retryable
	public Publisher<GokbScrollResponse> scrollTipps(@Nullable String scrollId, @Nullable Instant changedSince) {
		return scroll(COMPONENT_TYPE_TIPP, scrollId, changedSince != null ? changedSince.truncatedTo(ChronoUnit.SECONDS).toString() : null);
	}

	@SingleResult
	@Retryable
	public Publisher<GokbScrollResponse> scrollPackages(@Nullable String scrollId, @Nullable Instant changedSince) {
		return scroll(COMPONENT_TYPE_PACKAGE, scrollId, changedSince != null ? changedSince.truncatedTo(ChronoUnit.SECONDS).toString() : null);
	}

	@SingleResult
	@Retryable
	public Publisher<GokbScrollResponse> scroll(
		@NonNull @NotBlank @QueryValue(QUERY_PARAM_COMPONENT_TYPE) String type,
		@Nullable @QueryValue(SCROLL_ID_QUERY_PARAM) String scrollId,
		@Nullable @QueryValue(CHANGED_SINCE_QUERY_PARAM) String changedSince
	) {
		return get(
			SCROLL_URL,
			GokbScrollResponse.class,
			Optional.of(String.class),
			Optional.of(uri -> {
				uri.queryParam(QUERY_PARAM_COMPONENT_TYPE, type);
				uri.queryParam(SCROLL_ID_QUERY_PARAM, scrollId);
				uri.queryParam(CHANGED_SINCE_QUERY_PARAM, changedSince);
			}),
			Optional.empty()
		).map(resp -> resp.body());
	}
}