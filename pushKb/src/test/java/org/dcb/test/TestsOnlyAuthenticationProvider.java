package org.dcb.test;

import static io.micronaut.context.env.Environment.TEST;
import static io.micronaut.security.authentication.AuthenticationResponse.success;

import java.util.List;

import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Requires(env = {TEST})
@Primary
@Singleton
public class TestsOnlyAuthenticationProvider implements AuthenticationProvider<HttpRequest<?>> {
	@Override
	public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
		AuthenticationRequest<?, ?> authenticationRequest) {

		return Mono.just(success(
			authenticationRequest.getIdentity().toString(), List.of()));
	}
}
