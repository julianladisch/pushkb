package com.k_int.pushKb.vault;

import java.util.Map;

/**
 * Contract for retrieving secrets from a credential vault.
 * WARNING: This is from doc-del and should be kept backwards compatible with that implementation of the vault provider
 */
public interface VaultProvider {

	/**
	 * Ensure that the VaultProvider is capable of surfacing the config that led to its creation.
	 * @return vaultConfig
	 */
	VaultConfig getVaultConfig();
	/**
	 * Reads a secret at the given path.
	 *
	 * @param path path within the vault (e.g. "secret/data/my-service")
	 * @return secret payload
	 */
	VaultSecret readSecret(String path);

	boolean getVaultHealth();

	void createSecret(String path, Map<String, Object> secret);

	void updateSecret(String path, Map<String, Object> secret);

	void deleteSecret(String path);
}
