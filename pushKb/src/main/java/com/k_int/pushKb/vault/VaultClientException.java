package com.k_int.pushKb.vault;

import io.micronaut.http.HttpStatus;
import lombok.Getter;

/**
 * Represents a vault operation failure with HTTP semantic status for caller handling.
 * WARNING: This is from doc-del and should be kept backwards compatible with that implementation of the vault provider
 */
@Getter
public class VaultClientException extends RuntimeException {

	private final HttpStatus status;

	public VaultClientException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public VaultClientException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
}
