package com.k_int.pushKb.interactions.folio;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.cli.Option;
import io.micronaut.core.type.Argument;
import io.micronaut.data.connection.exceptions.ConnectionException;
import io.micronaut.http.BasicAuth;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.http.cookie.Cookie;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.retry.annotation.Retryable;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k_int.pushKb.interactions.HttpClientRequestResponseException;
import com.k_int.pushKb.utils.CookieToken;
import com.k_int.pushKb.utils.RelativeUriResolver;

import java.net.ConnectException;
import java.net.URI;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;
import static io.micronaut.http.HttpMethod.GET;
import static io.micronaut.http.HttpMethod.POST;
import static io.micronaut.http.HttpMethod.PUT;
import static io.micronaut.http.MediaType.APPLICATION_JSON;

public class FolioApiClient {
	public final static String X_OKAPI_TENANT = "X-Okapi-Tenant";
	public final static String FOLIO_ACCESS_TOKEN = "folioAccessToken";
	public final static String FOLIO_REFRESH_TOKEN = "folioRefreshToken";
	public final static String LOGIN_URI = "/authn/login-with-expiry";

	// PushKB Urls
	public final static String PUSHKB_BASE_URL = "/erm/pushKB";
	public final static String PUSHKB_PKG_URL = PUSHKB_BASE_URL + "/pkg";
	public final static String PUSHKB_PCI_URL = PUSHKB_BASE_URL + "/pci";

	private final HttpClient client;
	private final URI rootUri;
	private final String loginUser;
	private final String loginPassword;
	private final String tenant;

	// Keep a current token for login etc
	private CookieToken currentToken;

	// Logging
	static final Logger log = LoggerFactory.getLogger(FolioApiClient.class);

	public FolioApiClient(
		HttpClient client,
		String destinationUrl,
		String tenant,
		String loginUser,
		String loginPassword
	) {  
			this.client = client;
			this.tenant = tenant;
			this.loginUser = loginUser;
			this.loginPassword = loginPassword;

			rootUri = UriBuilder.of(destinationUrl)
							.build();
	}

	private <T> Mono<T> handleResponseErrors(final Mono<T> current, MutableHttpRequest<?> request) {
		return current.onErrorMap(exception -> {
			if (exception instanceof HttpClientResponseException) {
				// Return special HttpClientRequestResponseException which has access to both the exception and the original request
				return new HttpClientRequestResponseException((HttpClientResponseException)exception, request);
			}
			return exception;
		});
	}

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
					.header(X_OKAPI_TENANT, tenant)
				);
	}

	// Extra helpers to make code nicer *shrug* possibly just mess tbh but hey ho
	private <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Class<T> type, Class<?> errorType) {
		return doExchange(request, Argument.of(type), Optional.of(Argument.of(errorType)));
	}

	private <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Class<T> type) {
		return doExchange(request, Argument.of(type));
	}

	private <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Argument<T> type) {
		return doExchange(request, type, Optional.empty());
	}

	private <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Argument<T> type, Optional<Argument<?>> errorType) {
		if (errorType.isPresent()){
			return Mono.from(client.exchange(request, type, errorType.get())).transform(mono -> this.handleResponseErrors(mono, request));
		}
		return Mono.from(client.exchange(request, type)).transform(mono -> this.handleResponseErrors(mono, request));
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Class<T> type, Class<?> errorType) {
		return doRetrieve(request, Argument.of(type), Optional.of(Argument.of(errorType)));
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Class<T> type) {
		return doRetrieve(request, Argument.of(type));
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType) {
		return doRetrieve(request, argumentType, Optional.empty(), true);
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType, Optional<Argument<?>> errorType) {
		return doRetrieve(request, argumentType, Optional.empty(), true);
	}

	private <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType, Optional<Argument<?>> errorType, boolean mapErrors) {
		var response = errorType.isPresent() ? Mono.from(client.retrieve(request, argumentType, errorType.get())) : Mono.from(client.retrieve(request, argumentType));
		return mapErrors ? response.transform(mono -> this.handleResponseErrors(mono, request)) : response;
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

	@SingleResult
	public Mono<CookieToken> login() {
		FolioLoginBody loginBody = FolioLoginBody.builder()
																						 .username(loginUser)
																						 .password(loginPassword)
																						 .build();
		return postRequest(LOGIN_URI)
				.map(req -> {
					return req.body(loginBody);
				})
				// TODO Use authn/login-with-expiry, then we get expiration dates for tokens in response body (So response _would_ matter, grrr)
				.flatMap(req -> doExchange(
					req,
					FolioLoginResponseBody.class,
					FolioLoginError.class
				)) // Actual response doesn't matter, all Auth is in cookie headers
				.map(resp -> {
					Optional<FolioLoginResponseBody> flrb = resp.getBody(FolioLoginResponseBody.class);
					Optional<Cookie> accessTokenCookie = resp.getCookie(FOLIO_ACCESS_TOKEN);
					if (accessTokenCookie.isEmpty()) {
						// Is this right???
						throw new HttpClientException("No login cookie");
					}

					// If we have the response body and that contains an accessTokenExpiration, use it directly
					if (flrb.isEmpty() || flrb.get().getAccessTokenExpiration() == null) {
						return new CookieToken(accessTokenCookie.get());
					} else {
						return new CookieToken(accessTokenCookie.get(), flrb.get().accessTokenExpiration);
					}
				});
		// 
	}

	// TODO errorType is mandatory rn
	private <T> Mono<T> get(String path, Class<T> type, Class<?> errorType, Consumer<UriBuilder> uriBuilderConsumer) {
		return createRequest(GET, path).map(req -> req.uri(uriBuilderConsumer)).flatMap(this::ensureToken)
			.flatMap(req -> doRetrieve(req, type, errorType));
	}

	// Specific requests
	@SingleResult
	@Retryable
	public Publisher<String> getChunks() {
		return get("/erm/pushKB/chunks", String.class, String.class, uri -> {
			uri.queryParam("stats", true);
		});
	}

	@SingleResult
	@Retryable
	public Publisher<String> getAgreements() {
		return get("/erm/sas", String.class, String.class, uri -> {
			uri.queryParam("stats", true);
		});
	}
}