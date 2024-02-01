package com.k_int.pushKb.destinations.folio;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;

import io.micronaut.http.BasicAuth;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.http.cookie.Cookie;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.retry.annotation.Retryable;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k_int.pushKb.utils.CookieToken;
import com.k_int.pushKb.utils.RelativeUriResolver;

import java.net.URI;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;
import static io.micronaut.http.HttpMethod.GET;
import static io.micronaut.http.HttpMethod.POST;
import static io.micronaut.http.HttpMethod.PUT;
import static io.micronaut.http.MediaType.APPLICATION_JSON;


// DCB splits this into "generic" api client and then specific...
// Might be worth having a FOLIO client so we could then implement
// Destination/Source clients separately if we wished
public class FolioDestinationApiClient {
	private final static String X_OKAPI_TENANT = "X-Okapi-Tenant";
	private final static String FOLIO_ACCESS_TOKEN = "folioAccessToken";
	private final static String FOLIO_REFRESH_TOKEN = "folioRefreshToken";

	private final HttpClient client;
	private final URI rootUri;
	private final FolioDestination destination;

	// Keep a current token for login etc
	private CookieToken currentToken;

	// Logging
	static final Logger log = LoggerFactory.getLogger(FolioDestinationApiClient.class);

	public FolioDestinationApiClient(
		HttpClient client,
		FolioDestination destination
	) {  
			this.client = client;
			this.destination = destination;

			rootUri = UriBuilder.of(destination.getDestinationUrl())
							.build();
	}

	// LOGIN/TOKEN STUFF
	// FIXME we need error handling -- See HostLmsSierraApiClient for examples
/* 	private <T> Mono<T> handleResponseErrors(final Mono<T> current) {
		// We used to do
		// .transform(this::handle404AsEmpty)
		// Immediately after current, but some downstream chains rely upon the 404 so
		// for now we use .transform directly in the caller
		return current.doOnError(sierraResponseErrorMatcher::isUnauthorised, _t -> clearToken());
	} */

	private void clearToken() {
		log.debug("Clearing token to trigger re-authentication");
		this.currentToken = null;
	}

	private <T> Mono<MutableHttpRequest<T>> postRequest(String path) {
		return createRequest(POST, path);
	}

	private <T> Mono<MutableHttpRequest<T>> putRequest(String path) {
		return createRequest(PUT, path);
	}

	private <T> Mono<MutableHttpRequest<T>> createRequest(HttpMethod method, String path) {
		return Mono.just(UriBuilder.of(path).build()).map(this::resolve)
				.map(resolvedUri -> HttpRequest.<T>create(method, resolvedUri.toString())
					.accept(APPLICATION_JSON)
					.header(X_OKAPI_TENANT, destination.getTenant())
				);
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType) {
		return doRetrieve(request, argumentType, true);
	}

	private <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Class<T> type) {
		return Mono.from(client.exchange(request, Argument.of(type)));
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType, boolean mapErrors) {
		return Mono.from(client.retrieve(request, argumentType));
	}

	private URI resolve(URI relativeURI) {
		return RelativeUriResolver.resolve(rootUri, relativeURI);
	}

	private <T> Mono<MutableHttpRequest<T>> ensureToken(MutableHttpRequest<T> request) {
		return Mono.justOrEmpty(currentToken).filter(token -> !token.isExpired()).switchIfEmpty(acquireAccessToken())
				.map(validToken -> {
					// Uncomment to get token information in log
					//log.debug("Using Cookie token: {}", validToken);

					return request.cookie(validToken.returnToCookie());
				}).defaultIfEmpty(request);
	}

	private Mono<CookieToken> acquireAccessToken() {
		return Mono.from(login()).map(newToken -> {
			currentToken = newToken;
			return newToken;
		});
	}

	//@Override
	@SingleResult
	public Mono<CookieToken> login() {
		// FIXME There has to be a better way than this...
		String loginBody = "{\"username\":\"" + destination.getLoginUser() + "\",\"password\":\""+ destination.getLoginPassword() + "\"}";
		//String loginBody = "{\"username\":\"" + destination.getLoginUser() + "\",\"password\":\"wrong-password\"}";
		return postRequest("/bl-users/login-with-expiry")
				.map(req -> {
					return req.body(loginBody);
				})
				// FIXME needs error handling for bad login creds
				.flatMap(req -> doExchange(req, Object.class))
				.map(resp -> {
					// Get hold of cookie from resp
					return new CookieToken(resp.getCookie(FOLIO_ACCESS_TOKEN).get());
				});
		// 
	}

	private <T> Mono<T> get(String path, Argument<T> argumentType, Consumer<UriBuilder> uriBuilderConsumer) {
		return createRequest(GET, path).map(req -> req.uri(uriBuilderConsumer)).flatMap(this::ensureToken)
				.flatMap(req -> doRetrieve(req, argumentType));
	}

	// Specific requests
	@SingleResult
	@Retryable
	public Publisher<String> getChunks() {
		return get("/erm/pushKB/chunks", Argument.of(String.class), uri -> {
			uri.queryParam("stats", true);
		});
	}

	@SingleResult
	@Retryable
	public Publisher<String> getAgreements() {
		return get("/erm/sas", Argument.of(String.class), uri -> {
			uri.queryParam("stats", true);
		});
	}
}