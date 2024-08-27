package com.k_int.pushKb.utils;

import java.time.Duration;
import java.time.Instant;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.serde.annotation.Serdeable;

// This is a record to handle any cases where authentication is handled via cookies (FOLIO)
@Serdeable
public record CookieToken(
	@NotNull String name,
	@NotEmpty String value,
	@NotNull Instant expires
) {

	public static final Duration DEFAULT_EXPIRATION_BUFFER = Duration.ofSeconds(10);
	
	// If we don't have an expiry date explicitly (Because cookies don't actually hold those) then make best guess with 10 second buffer
	public CookieToken(
		@NotEmpty String name,
		@NotEmpty String value,
		@NotEmpty long maxAge) {
		this(name, value, Instant.now().plus(Duration.parse("PT" + maxAge + "S")).minus(DEFAULT_EXPIRATION_BUFFER));
	}

	public CookieToken(
		Cookie cookie
	) {
		this(cookie.getName(), cookie.getValue(), cookie.getMaxAge());
	}

	public CookieToken(
		Cookie cookie,
		Instant expires
	) {
		this(cookie.getName(), cookie.getValue(), expires);
	}

	public boolean isExpired() {
		return expires.isBefore(Instant.now());
	}

	@Override
	public String toString() {
		return String.format("%s %s", this.name, this.value);
	}

	/* 
	 * Bit hacky but this returns us from a controlled CookieToken
	 * (where we have direct expiry data etc) back to a Cookie we
	 * can glue onto a request
	 */
	public Cookie returnToCookie() {
		return new NettyCookie(this.name, this.value);
	}
}
