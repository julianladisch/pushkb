package com.k_int.pushKb.interactions.folio;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.retry.annotation.Retryable;
import jakarta.validation.OverridesAttribute;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k_int.pushKb.interactions.BaseApiClient;
import com.k_int.pushKb.interactions.folio.model.FolioLoginBody;
import com.k_int.pushKb.interactions.folio.model.FolioLoginError;
import com.k_int.pushKb.interactions.folio.model.FolioLoginResponseBody;
import com.k_int.pushKb.utils.CookieToken;

import static io.micronaut.http.HttpMethod.DELETE;
import static io.micronaut.http.HttpMethod.GET;
import static io.micronaut.http.HttpMethod.POST;
import static io.micronaut.http.HttpMethod.PUT;

import java.util.Optional;
import java.util.function.Consumer;

public class FolioApiClient extends BaseApiClient {
	public final static String X_OKAPI_TENANT = "X-Okapi-Tenant";
	public final static String FOLIO_ACCESS_TOKEN = "folioAccessToken";
	public final static String FOLIO_REFRESH_TOKEN = "folioRefreshToken";

	// Static paths
	// Auth Urls
	public final static String LOGIN_URI = "/authn/login-with-expiry";

	// PushKB Urls
	public final static String PUSHKB_BASE_URL = "/erm/pushKB";
	public final static String PUSHKB_PKG_URL = PUSHKB_BASE_URL + "/pkg";
	public final static String PUSHKB_PCI_URL = PUSHKB_BASE_URL + "/pci";

	private final String loginUser;
	private final String loginPassword;
	private final String tenant;

	// Keep a current token for login etc
	private CookieToken currentToken;

	// Logging
	static final Logger log = LoggerFactory.getLogger(FolioApiClient.class);

	public FolioApiClient(
		HttpClient client,
		String tenant,
		String loginUser,
		String loginPassword
	) {
		super(client);
		this.tenant = tenant;
		this.loginUser = loginUser;
		this.loginPassword = loginPassword;
	}

/* 	private void clearToken() {
		log.debug("Clearing token to trigger re-authentication");
		this.currentToken = null;
	} */

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

	private Optional<Consumer<MutableHttpHeaders>> setFolioHeaders(Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer) {
		return Optional.of(headers -> {
			headers.set(FolioApiClient.X_OKAPI_TENANT, tenant);
			if (httpHeaderConsumer.isPresent()) {
				httpHeaderConsumer.get().accept(headers);
			}
		});
	}

	// Override createRequest by adding in token logic and folio tenant header
	@Override
	protected Mono<MutableHttpRequest<?>> createRequest(
    HttpMethod method,
    String path,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {
		return super.createRequest(method, path, uriBuilderConsumer, setFolioHeaders(httpHeaderConsumer)).flatMap(req -> ensureToken(req));
	}

	@SingleResult
	public Mono<CookieToken> login() {
		FolioLoginBody loginBody = FolioLoginBody.builder()
																						 .username(loginUser)
																						 .password(loginPassword)
																						 .build();
		return post( // Don't use our internal createRequest because that requires token, which this is trying to set.
			super.createRequest(POST, LOGIN_URI, Optional.empty(), setFolioHeaders(Optional.empty())),
			FolioLoginResponseBody.class,
			Optional.of(loginBody),
			Optional.of(FolioLoginError.class)
		)
			// Actual response doesn't matter, all Auth is in cookie headers
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
					return new CookieToken(accessTokenCookie.get(), flrb.get().getAccessTokenExpiration());
				}
			});
	}

	// Specific requests
	@SingleResult
	@Retryable
	public Publisher<String> getChunks() {
		return get(
			"/erm/pushKB/chunks",
			String.class,
			Optional.of(String.class),
			Optional.of(uri -> {
				uri.queryParam("stats", true);
			}),
			Optional.empty()
		).map(resp -> resp.body());
	}

	// Specific requests
	@SingleResult
	@Retryable
	public Publisher<String> getAgreements() {
		return get(
			"/erm/sas",
			String.class,
			Optional.of(String.class),
			Optional.of(uri -> {
				uri.queryParam("stats", true);
			}),
			Optional.empty()
		).map(resp -> resp.body());
	}
}