package com.k_int.pushKb.interactions.gokb;

import static io.micronaut.http.HttpHeaders.*;
import static io.micronaut.http.HttpMethod.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class GokbApiClient extends BaseApiClient {
	public final static String GOKB_USER_AGENT = "pushKB";

	public static final String COMPONENT_TYPE_TIPP = "TitleInstancePackagePlatform";
	public static final String COMPONENT_TYPE_PACKAGE = "Package";

	// Append relative instead of absolute -- Trusting that the HttpClient client is set up with `/gokb/api/` ahead of time
	public static final String SCROLL_URL = "scroll";
	public static final String SCROLL_ID_QUERY_PARAM = "scrollId";
	public static final String SORT_QUERY_PARAM = "sort";
	public static final String CHANGED_SINCE_QUERY_PARAM = "changedSince";

	public static final String QUERY_PARAM_COMPONENT_TYPE = "component_type";

	public static final String LAST_UPDATED_DISPLAY = "lastUpdatedDisplay";

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
					headers.set(USER_AGENT, GokbApiClient.GOKB_USER_AGENT);
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
		log.debug("CHANGEDSINCE: {}", changedSince != null ? changedSince.truncatedTo(ChronoUnit.SECONDS).toString() : null);
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
		log.info("SENDING SCROLL REQUEST WITH CHANGEDSINCE: {} AND SCROLL ID: {}", changedSince, scrollId);
		return get(
			SCROLL_URL,
			GokbScrollResponse.class,
			Optional.of(String.class),
			Optional.of(uri -> {
				uri.queryParam(QUERY_PARAM_COMPONENT_TYPE, type);
				uri.queryParam(SCROLL_ID_QUERY_PARAM, scrollId);
				uri.queryParam(CHANGED_SINCE_QUERY_PARAM, changedSince);
				uri.queryParam(SORT_QUERY_PARAM, LAST_UPDATED_DISPLAY);
			}),
			Optional.empty()
		).map(resp -> resp.body());
	}
}