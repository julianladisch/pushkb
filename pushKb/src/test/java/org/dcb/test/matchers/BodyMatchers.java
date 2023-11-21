package org.dcb.test.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.micronaut.http.client.exceptions.HttpClientResponseException;

public class BodyMatchers {
	public static void hasTextBody(HttpClientResponseException exception,
		String expectedText) {

		assertThat("Response should not be null", exception.getResponse(),
			is(notNullValue()));

		final var body = exception.getResponse().getBody(String.class);

		assertThat("Body should not be null", body, is(notNullValue()));
		assertThat("Body should be present", body.isPresent(), is(true));
		assertThat("Response should have a explanatory message",
			body.get(), is(expectedText));
	}
}
