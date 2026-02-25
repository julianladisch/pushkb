package com.k_int.pushKb;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
	info = @Info(
		title = "PushKb",
		version = "1.0.0-SNAPSHOT",
		description = "A microservice to connect \"sources\" to \"destinations\" and act as a mediator for streaming data between the two. " +
			"Initial implementation focuses on FOLIO destinations and GOKB sources, replacing the \"harvest\" mechanism in ERM.",
		license = @License(name = "Apache 2.0", url = "https://gitlab.com/knowledge-integration/libraries/pushkb/-/blob/main/LICENSE"),
		contact = @Contact(url = "https://www.k-int.com", name = "Ethan Freestone", email = "ethan.freestone@k-int.com")
	)
)
@SecurityScheme(
	name = "Keycloak",
	type = SecuritySchemeType.OAUTH2,
	description = "Login with your PushKB Keycloak credentials",
	flows = @OAuthFlows(
		password = @OAuthFlow(
			tokenUrl = "${micronaut.security.oauth2.clients.keycloak.openid.issuer}/protocol/openid-connect/token",
			scopes = {
				@OAuthScope(name = "openid", description = "Standard OIDC"),
				@OAuthScope(name = "profile", description = "User profile info")
			}
		)
	)
)
public class Application {
	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}
}
