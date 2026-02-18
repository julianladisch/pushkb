package com.k_int.pushKb.interactions.folio.services;

import com.k_int.pushKb.interactions.folio.FolioApiClient;
import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.services.HttpClientService;
import com.k_int.pushKb.vault.VaultProvider;
import com.k_int.pushKb.vault.VaultSecret;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.http.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FolioDestinationApiServiceTest {

	private HttpClientService httpClientService;
	private VaultProvider vaultProvider;
	private HttpClient mockClient;

	@BeforeEach
	void setup() {
		httpClientService = mock(HttpClientService.class);
		vaultProvider = mock(VaultProvider.class);
		mockClient = mock(HttpClient.class);
	}

	private static Stream<Arguments> getClientTestArguments() {
		return Stream.of(
			// 1. Vault Healthy, Password Found -> Result should have Vault password
			Arguments.of(
				Named.of("Standard OKAPI Auth - Vault Injects Password", FolioAuthType.OKAPI),
				true,  // vaultHealthy
				false, // insecureMode
				Map.of("password", "vault-secret-pass"), // vaultData
				"vault-secret-pass", // expectedPassword
				null   // expectedError
			),
			// 2. Vault Healthy, No Password key -> Result should have original tenant state (null password)
			Arguments.of(
				Named.of("No Password in Vault (Auth NONE) - Returns Original", FolioAuthType.NONE),
				true,
				false,
				Map.of(),
				null, // expectedPassword
				null
			),
			// 3. Vault Down, Insecure Mode OFF -> Exception
			Arguments.of(
				Named.of("Vault Down - Secure Mode - Throws Exception", FolioAuthType.OKAPI),
				false,
				false,
				Map.of(),
				null,
				IllegalStateException.class
			),
			// 4. Vault Down, Insecure Mode ON -> Result should have original password from DB object
			Arguments.of(
				Named.of("Vault Down - Insecure Fallback - Uses DB Password", FolioAuthType.OKAPI),
				false,
				true,
				Map.of(),
				"original-db-pass", // expectedPassword
				null
			)
		);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("getClientTestArguments")
	@DisplayName("FolioDestinationApiService.getClient handles various Vault and Auth states")
	void testGetClient(
		FolioAuthType authType,
		boolean vaultHealthy,
		boolean insecureMode,
		Map<String, Object> vaultData,
		String expectedPassword,
		Class<? extends Throwable> expectedError
	) throws MalformedURLException {

		// ARRANGE
		FolioDestinationApiService service = new FolioDestinationApiService(httpClientService, vaultProvider, insecureMode);

		FolioTenant tenant = FolioTenant.builder()
			.id(UUID.randomUUID())
			.tenant("test_tenant")
			.baseUrl("http://folio.example.com")
			.authType(authType)
			.loginUser(authType == FolioAuthType.NONE ? null : "original-db-username")
			.loginPassword(authType == FolioAuthType.NONE ? null : "original-db-pass")
			.build();

		FolioDestination dest = FolioDestination.builder()
			.folioTenant(tenant)
			.build();

		when(vaultProvider.getVaultHealth()).thenReturn(vaultHealthy);
		when(vaultProvider.readSecret(any())).thenReturn(new VaultSecret(vaultData));
		when(httpClientService.create((URL) any())).thenReturn(mockClient);

		// ACT
		var publisher = service.getClient(dest);

		// ASSERT
		if (expectedError != null) {
			StepVerifier.create(publisher)
				.expectError(expectedError)
				.verify();
		} else {
			StepVerifier.create(Mono.from(publisher))
				.assertNext(apiClient -> {
					assertNotNull(apiClient);

					// Use reflection in test to access internal private field -- we don't want the whole world
					// seeing the folioTenant inside of the client as it has unsanitised password credentials
					FolioTenant internalTenant = (FolioTenant) ReflectionUtils.getField(FolioApiClient.class, "folioTenant", apiClient);
					assertEquals(expectedPassword, internalTenant.getLoginPassword());

					assertEquals(expectedPassword, internalTenant.getLoginPassword(),
						"The tenant inside the ApiClient should have the correct password state");
				})
				.verifyComplete();
		}
	}
}
