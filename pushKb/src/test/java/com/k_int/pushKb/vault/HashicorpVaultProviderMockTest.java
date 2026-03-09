package com.k_int.pushKb.vault;

import io.micronaut.serde.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HashicorpVaultProviderMockTest {

	private final ObjectMapper objectMapper = ObjectMapper.getDefault();
	private HttpClient mockHttpClient;
	private HttpResponse mockResponse;
	private VaultConfig.HashicorpConfig hashicorpConfig;
	private VaultConfig vaultConfig;
	private VaultConfig.HashicorpConfig.KubernetesConfig kubernetesConfig;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		mockHttpClient = mock(HttpClient.class);
		mockResponse = mock(HttpResponse.class);
		vaultConfig = mock(VaultConfig.class);
		hashicorpConfig = mock(VaultConfig.HashicorpConfig.class);
		kubernetesConfig = mock(VaultConfig.HashicorpConfig.KubernetesConfig.class);

		when(vaultConfig.getHashicorpConfig()).thenReturn(hashicorpConfig);
		when(hashicorpConfig.getUrl()).thenReturn("http://mock-vault:8200");
		when(hashicorpConfig.getSecretEnginePath()).thenReturn("/v1/secret/data/");
		when(hashicorpConfig.getKubernetesConfig()).thenReturn(kubernetesConfig);

		// Default to token auth to prevent login loops in unrelated tests
		when(hashicorpConfig.getAuthtype()).thenReturn("token");
		when(hashicorpConfig.getToken()).thenReturn(Optional.of("test-token"));
	}

	private HashicorpVaultProvider createProvider() throws Exception {
		HashicorpVaultProvider provider = new HashicorpVaultProvider(objectMapper, vaultConfig);
		Field field = HashicorpVaultProvider.class.getDeclaredField("httpClient");
		field.setAccessible(true);
		field.set(provider, mockHttpClient);
		return provider;
	}

	@Test
	@DisplayName("Test Kubernetes Auth Flow - Success with Login and Secret Call")
	void testKubernetesAuthSuccess() throws Exception {
		// 1. Setup K8s environment
		Path tokenFile = tempDir.resolve("token");
		Files.writeString(tokenFile, "mock-sa-jwt-token");

		when(hashicorpConfig.getAuthtype()).thenReturn("kubernetes");
		when(hashicorpConfig.getToken()).thenReturn(Optional.empty());
		when(kubernetesConfig.getServiceAccountTokenPath()).thenReturn(Optional.of(tokenFile.toString()));
		when(kubernetesConfig.getMountPath()).thenReturn(Optional.of("kubernetes"));
		when(kubernetesConfig.getRole()).thenReturn(Optional.of("my-role"));

		// 2. Prepare the Two Different Responses
		String loginJsonResponse = """
    {
      "auth": {
        "client_token": "valid-session-token"
      }
    }
    """;

		String secretJsonResponse = """
    {
      "data": {
        "data": {
          "key": "value"
        }
      }
    }
    """;

		// Create two distinct response mocks
		HttpResponse<byte[]> loginResponse = mock(HttpResponse.class);
		when(loginResponse.statusCode()).thenReturn(200);
		when(loginResponse.body()).thenReturn(loginJsonResponse.getBytes());

		HttpResponse<byte[]> secretResponse = mock(HttpResponse.class);
		when(secretResponse.statusCode()).thenReturn(200);
		when(secretResponse.body()).thenReturn(secretJsonResponse.getBytes());

		// 3. STUB CONSECUTIVELY: 1st call = Login, 2nd call = Secret
		when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
			.thenReturn(loginResponse)
			.thenReturn(secretResponse);

		HashicorpVaultProvider provider = createProvider();

		// 4. Execution
		VaultSecret result = provider.readSecret("my-secret");

		// 5. Verifications
		assertNotNull(result.data());
		assertEquals("value", result.data().get("key"));

		// Verify exactly 2 calls were made
		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(mockHttpClient, times(2)).send(captor.capture(), any());

		// Verify 1st call was Login (POST), 2nd call was Secret (GET)
		assertEquals("POST", captor.getAllValues().get(0).method());
		assertEquals("GET", captor.getAllValues().get(1).method());

		Optional<String> tokenValue = captor.getAllValues().get(1).headers().firstValue("X-Vault-Token");
		assertTrue(tokenValue.isPresent());

		assertEquals("valid-session-token", tokenValue.get());
	}

	@Test
	@DisplayName("Test Login Error Logging - Coverage for logLoginError")
	void testLoginErrorLogging() throws Exception {
		when(hashicorpConfig.getAuthtype()).thenReturn("userpass");
		when(hashicorpConfig.getToken()).thenReturn(Optional.empty());
		when(hashicorpConfig.getUsername()).thenReturn(Optional.of("wrong-user"));
		when(hashicorpConfig.getPassword()).thenReturn(Optional.of("wrong-pass"));

		// Mock a 401 Unauthorized with the Vault error format
		String errorJson = "{\"errors\": [\"invalid username or password\"]}";
		when(mockResponse.statusCode()).thenReturn(401);
		when(mockResponse.body()).thenReturn(errorJson.getBytes());
		when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

		HashicorpVaultProvider provider = createProvider();

		// This should trigger resolveClientToken -> logLoginError -> Throw Exception
		VaultClientException ex = assertThrows(VaultClientException.class, () -> provider.readSecret("any"));
		assertEquals(401, ex.getStatus().getCode());
	}

	@Test
	@DisplayName("Test Unsupported Auth Type")
	void testUnsupportedAuthType() throws Exception {
		when(hashicorpConfig.getAuthtype()).thenReturn("unsupported-type");
		when(hashicorpConfig.getToken()).thenReturn(Optional.empty());

		HashicorpVaultProvider provider = createProvider();

		VaultClientException ex = assertThrows(VaultClientException.class, () -> provider.readSecret("test"));
		assertTrue(ex.getMessage().contains("do not currently support authType unsupported-type"));
	}

	@ParameterizedTest
	@DisplayName("Test Health Check Status Codes")
	@ValueSource(ints = {429, 472, 473, 474, 501, 503, 530})
	void testHealthCheckFailures(int statusCode) throws Exception {
		HashicorpVaultProvider provider = createProvider();
		when(mockResponse.statusCode()).thenReturn(statusCode);
		when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

		assertFalse(provider.getVaultHealth(), "Status " + statusCode + " should return false");
	}

	@Test
	@DisplayName("Test Error Logging with JSON Response")
	void testErrorLoggingLogic() throws Exception {
		HashicorpVaultProvider provider = createProvider();

		String errorJson = "{\"errors\": [\"permission denied\"]}";
		when(mockResponse.statusCode()).thenReturn(403);
		when(mockResponse.body()).thenReturn(errorJson.getBytes());
		when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

		assertThrows(VaultClientException.class, () -> provider.readSecret("secret/test"));
	}

	@Test
	@DisplayName("Test Userpass Auth Flow")
	void testUserpassAuth() throws Exception {
		when(hashicorpConfig.getAuthtype()).thenReturn("userpass");
		when(hashicorpConfig.getToken()).thenReturn(Optional.empty());
		when(hashicorpConfig.getUsername()).thenReturn(Optional.of("test-user"));
		when(hashicorpConfig.getPassword()).thenReturn(Optional.of("test-pass"));

		HashicorpVaultProvider provider = createProvider();

		String loginJson = "{\"auth\": {\"client_token\": \"session-token-123\"}}";
		when(mockResponse.statusCode()).thenReturn(200);
		when(mockResponse.body()).thenReturn(loginJson.getBytes());
		when(mockHttpClient.send(any(), any())).thenReturn(mockResponse);

		provider.deleteSecret("some-path");

		ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(mockHttpClient, atLeastOnce()).send(requestCaptor.capture(), any());
		assertTrue(requestCaptor.getAllValues().get(0).uri().toString().contains("auth/userpass/login/test-user"));
	}
}
