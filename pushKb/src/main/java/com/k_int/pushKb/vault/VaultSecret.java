package com.k_int.pushKb.vault;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Collections;
import java.util.Map;

/**
 * Value object representing a vault secret payload.
 * WARNING: This is from doc-del and should be kept backwards compatible with that implementation of the vault provider
 */
@Serdeable
public record VaultSecret(Map<String, Object> data) {

	public VaultSecret {
		data = data != null ? Map.copyOf(data) : Collections.emptyMap();
	}
}
