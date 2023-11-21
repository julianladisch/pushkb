package org.dcb.test.clients;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;

import jakarta.inject.Singleton;

@Singleton
public class LoginClient {
	private final HttpClient httpClient;

	public LoginClient(@Client("/") HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public String login() {
		// Uses a test-only authentication provider
		final var credentials = new UsernamePasswordCredentials("user", "password");

		final var request = HttpRequest.POST("/login", credentials);

		final var response = httpClient.toBlocking()
			.exchange(request, BearerAccessRefreshToken.class);

		return response.getBody()
			.orElseThrow(() -> new RuntimeException("No token provided in response"))
			.getAccessToken();
	}
}
