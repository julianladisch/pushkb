package com.k_int.pushKb.vault;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpStatus;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple HashiCorp Vault client using JDK HttpClient.
 * WARNING: This is from doc-del and should be kept backwards compatible with that implementation of the vault provider
 */
@Singleton
@Slf4j
public class HashicorpVaultProvider implements VaultProvider {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String configuredToken;
	private final String authType;
	private final String secretEnginePath;
	private final String username;
	private final String password;
	private final String kubernetesAuthRole;
	private final String kubernetesAuthMountPath;
	private final Path kubernetesTokenPath;
	private final AtomicReference<String> cachedToken = new AtomicReference<>();

	private final VaultConfig vaultConfig;

	public HashicorpVaultProvider(
		ObjectMapper objectMapper,
		VaultConfig vaultConfig
	) {
		VaultConfig.HashicorpConfig hashicorpConfig = vaultConfig.getHashicorpConfig();

		this.objectMapper = objectMapper;
		this.baseUrl = normalizeBaseUrl(hashicorpConfig.getUrl());
		this.authType = hashicorpConfig.getAuthtype();
		this.secretEnginePath = hashicorpConfig.getSecretEnginePath();
		this.configuredToken = hashicorpConfig.getToken().orElse("");
		//userpass credentials
		this.username = hashicorpConfig.getUsername().orElse(null);
		this.password = hashicorpConfig.getPassword().orElse(null);
		//kubernetes credentials
		VaultConfig.HashicorpConfig.KubernetesConfig kubernetesConfig = hashicorpConfig.getKubernetesConfig();

		this.kubernetesAuthRole = kubernetesConfig.getRole().orElse(null);
		this.kubernetesAuthMountPath = kubernetesConfig.getMountPath().orElse(null);

		Optional<String> theKubernetesTokenPath = kubernetesConfig.getServiceAccountTokenPath();
		this.kubernetesTokenPath = theKubernetesTokenPath.map(Path::of).orElse(null);
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

		this.vaultConfig = vaultConfig;
	}

	@Override
	public VaultConfig getVaultConfig() {
		return vaultConfig;
	}

	@Override
	public boolean getVaultHealth() throws VaultClientException {
		String path = "sys/health";
		String uri = baseUrl + "/v1/" + path;
		log.info("Checking vault health : {}/v1/{}", baseUrl, path);
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(uri))
			.GET()
			.build();

		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			int status = response.statusCode();
			switch(status) {
				case 200: {
					log.info("Vault status : initialised, unsealed and active");
					return true;
				}
				case 429:{
					log.warn("Vault status : unsealed and standby");
					return false;
				}
				case 472: {
					log.warn("Vault status : disaster recovery secondary");
					return false;
				}
				case 473: {
					log.warn("Vault status : performance standby");
					return false;
				}
				case 474: {
					log.warn("Vault status : standby node but cannot connect to the active node");
					return false;
				}
				case 501: {
					log.warn("Vault status : not initialized");
					return false;
				}
				case 503: {
					log.warn("Vault status : sealed");
					return false;
				}
				case 530: {
					log.warn("Vault status : removed");
					return false;
				}
			}
			if (status == HttpStatus.NOT_FOUND.getCode()) {
				log.error("Vault health check path not found: {}", path);
				throw new VaultClientException(HttpStatus.NOT_FOUND, "Vault path not found: " + path);
			}
			return false;
		} catch (IOException e) {
			log.error("Vault request failed for {}/v1/{}", baseUrl, path, e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault IO Failure", e);
		} catch (InterruptedException e) {
				log.error("Vault request interrupted for {}/v1/{}", baseUrl, path, e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault request interrupted", e);
		} catch (Exception e) {
			log.error("Unexpected Vault error in request for {}/v1/{}", baseUrl, path, e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Vault failure", e);
		}
	}

	@Override
	public VaultSecret readSecret(String path) {

		log.info("Attempt vault connection : {}{}{}",baseUrl, secretEnginePath, path);

		String clientToken = resolveClientToken();
		// Gonna have to figure out kv engine versioning
		String uri = baseUrl + secretEnginePath + path;
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(uri))
			.header("X-Vault-Token", clientToken)
			.header("Accept", "application/json")
			.GET()
			.build();
		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			int status = response.statusCode();
			// 404 Can also mean that a secret is not present
			if (status == HttpStatus.NOT_FOUND.getCode()) {
				log.error("Vault secret path not found: {}", path);
				return new VaultSecret(null);
			}
			if (status >= 400) {
				// 404 when reading a secret can also mean that a status has not been written, handle this accordingly
				String errorDetails = logVaultErrorResponse(path, status, response.body());
				if (status == HttpStatus.FORBIDDEN.getCode()) {
					String message = errorDetails != null && !errorDetails.isBlank()
						? "Vault secret request forbidden: " + errorDetails
						: "Vault secret request forbidden";
					throw new VaultClientException(HttpStatus.FORBIDDEN, message);
				}
				throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault secret request failed with status " + status);
			}

			Map<String, Object> root = objectMapper.readValue(response.body(), Argument.mapOf(String.class, Object.class));
			Map<String, Object> dataWrapper = asMap(root.get("data"));
			Map<String, Object> data = Optional.ofNullable(asMap(dataWrapper.get("data")))
				.orElse(dataWrapper);
			return new VaultSecret(data);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Vault secret request failed for {}{}{}", baseUrl, secretEnginePath, path, e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault secret request failed", e);
		}
	}

	@Override
	public void createSecret(String path, Map<String, Object> secret) {
		String clientToken = resolveClientToken();
		// Gonna have to figure out kv engine versioning
		String uri = baseUrl + secretEnginePath + path;
		try {
			byte[] body = objectMapper.writeValueAsBytes(secret);
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.timeout(Duration.ofSeconds(5))
				.header("X-Vault-Token", clientToken)
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofByteArray(body))
				.build();
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() >= 400) {
				throw new VaultClientException(HttpStatus.UNAUTHORIZED, "Vault secret create failed with status " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Vault secret creation failed", e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault secret creation failed", e);
		}
	}

	@Override
	public void updateSecret(String path, Map<String, Object> secret) {
		// Hashicorp vault considers POST and PUT to be synonyms
		createSecret(path, secret);
	}

	@Override
	public void deleteSecret(String path) {
		String clientToken = resolveClientToken();
		String uri = baseUrl + secretEnginePath + path;
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(uri))
			.header("X-Vault-Token", clientToken)
			.DELETE()
			.build();
		try {
			HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
			if (response.statusCode() >= 400) {
				throw new VaultClientException(HttpStatus.valueOf(response.statusCode()), "Vault delete failed");
			}
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault delete failed", e);
		}
	}

	private String resolveClientToken() {
		String loginPath;
		Map<String, Object> payload;

		String current = cachedToken.get();
		if (current != null && !current.isBlank()) {
			return current;
		}

		if (!configuredToken.isBlank()) {
			cachedToken.compareAndSet(null, configuredToken);
			return configuredToken;
		}
		switch(authType) {
			case ("kubernetes"):{
				log.info("resolveClientToken {}/v1/auth/{}/login", baseUrl, kubernetesAuthMountPath);

				String saToken = readKubernetesServiceAccountToken();
				if (saToken == null || saToken.isBlank()) {
					throw new VaultClientException(HttpStatus.UNAUTHORIZED, "Vault token not configured and no Kubernetes service account token available");
				}
				loginPath = baseUrl + "/v1/auth/" + kubernetesAuthMountPath + "/login";
				payload = Map.of(
					"role", kubernetesAuthRole,
					"jwt", saToken
				);
				break;
			}
			case("userpass"):{
				log.info("resolveClientToken {}/v1/auth/userpass/login/{}",baseUrl,username);
				loginPath = baseUrl + "/v1/auth/userpass/login/" + username;
				payload = Map.of(
					"password", password
				);
				break;
			}
			default:{
				throw new VaultClientException(HttpStatus.UNAUTHORIZED, "We do not currently support authType " + authType);
			}
		}

		try {
			byte[] body = objectMapper.writeValueAsBytes(payload);
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(loginPath))
				.timeout(Duration.ofSeconds(5))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofByteArray(body))
				.build();
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() >= 400) {
				logLoginError(loginPath, response.statusCode(), response.body());
				throw new VaultClientException(HttpStatus.UNAUTHORIZED, "Vault login failed with status " + response.statusCode());
			}
			Map<String, Object> root = objectMapper.readValue(response.body(), Argument.mapOf(String.class, Object.class));
			Map<String, Object> data = asMap(root.get("auth"));
			String clientToken = data != null ? Optional.ofNullable(data.get("client_token")).map(Object::toString).orElse(null) : null;
			if (clientToken == null || clientToken.isBlank()) {
				log.error("Vault login did not return a client token");
				throw new VaultClientException(HttpStatus.UNAUTHORIZED, "Vault login did not return a client token");
			}
			cachedToken.set(clientToken);
			return clientToken;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Vault login failed", e);
			throw new VaultClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Vault login failed", e);
		}
	}

	private String readKubernetesServiceAccountToken() {
		try {
			if (Files.exists(kubernetesTokenPath)) {
				log.info("Located token file at {}",kubernetesTokenPath);
				return Files.readString(kubernetesTokenPath).trim();
			}
		} catch (IOException e) {
			log.error("Failed reading Kubernetes service account token from {}", kubernetesTokenPath, e);
		}
		return null;
	}

	private String normalizeBaseUrl(String url) {

		log.info("normalizeBaseUrl({})",url);

		if (url == null || url.isBlank()) {
			return "http://localhost:8200";
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return null;
	}

	private void logLoginError(String loginPath, int status, byte[] responseBody) {
		try {
			Map<String, Object> errorRoot = objectMapper.readValue(responseBody, Argument.mapOf(String.class, Object.class));
			Object errors = errorRoot.get("errors");
			if (errors instanceof Iterable<?> iterable) {
				StringBuilder sb = new StringBuilder();
				for (Object error : iterable) {
					if (!sb.isEmpty()) {
						sb.append("; ");
					}
					sb.append(String.valueOf(error));
				}
				if (!sb.isEmpty()) {
					log.error("Vault {} login failed against {} for role {} with status {}. Errors: {}", authType, loginPath, kubernetesAuthRole, status, sb);
					return;
				}
			}
		} catch (IOException e) {
			log.debug("Failed to parse Vault login error response", e);
		}
		log.error("Vault {} login failed against {} for role {} with status {}", authType, loginPath, kubernetesAuthRole, status);
	}

	private String logVaultErrorResponse(String path, int status, byte[] responseBody) {
		try {
			Map<String, Object> errorRoot = objectMapper.readValue(responseBody, Argument.mapOf(String.class, Object.class));
			Object errors = errorRoot.get("errors");
			if (errors instanceof Iterable<?> iterable) {
				StringBuilder sb = new StringBuilder();
				for (Object error : iterable) {
					if (!sb.isEmpty()) {
						sb.append("; ");
					}
					sb.append(String.valueOf(error));
				}
				if (!sb.isEmpty()) {
					log.error("Vault request failed for {} with status {}. Errors: {}", path, status, sb);
					return sb.toString();
				}
			}
		} catch (IOException e) {
			log.debug("Failed to parse Vault error response", e);
		}
		log.error("Vault request failed for {} with status {}", path, status);
		return null;
	}
}
