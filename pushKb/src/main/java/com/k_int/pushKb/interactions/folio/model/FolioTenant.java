package com.k_int.pushKb.interactions.folio.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.k_int.pushKb.crud.HasId;

import com.k_int.pushKb.model.VaultEntity;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

import io.micronaut.serde.config.annotation.SerdeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import services.k_int.utils.UUIDUtils;

// Specifically store information for a FOLIO instance + tenant (login etc)
@Serdeable
@Data
@MappedEntity("folio_tenant")
@AllArgsConstructor
@Schema(
	name = "FolioTenant",
	description = "Detailed configuration for a specific FOLIO tenant environment"
)
@Builder(toBuilder = true)
public class FolioTenant implements HasId, VaultEntity {
  @Id
	@TypeDef(type = DataType.UUID)
	@Schema(
		description = "The unique identifier, automatically generated from the tenant and baseUrl.",
		accessMode = Schema.AccessMode.READ_ONLY // This hides it from the "Request Body" in Swagger
	)
	private UUID id;

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  protected String baseUrl;

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  private final String tenant;

  @NotNull
  @NonNull
  @ToString.Include
  @Size(max = 200)
  private final String name;

  // FIXME Creds shouold NOT be stored like this
  @Nullable
  @Size(max = 200)
  private final String loginUser;

  @Nullable
  @Size(max = 200)
  private final String loginPassword;

  // Track whether this FOLIO needs auth through okapi or not.
  @NotNull
  @NonNull
  private final FolioAuthType authType;

	@Transient
	@Override
	public String getKey(){

		// If we have an id already, use it, else generate one
		// (since it won't change after persist)
		if (id != null) {
			return "folioTenant/" + id.toString();
		}

		return "folioTenant/" + generateUUIDFromFolioTenant(this).toString();
	}

  private static final String UUID5_PREFIX = "folio_tenant";
  public static UUID generateUUID(String tenant, String baseUrl) {
    final String concat = UUID5_PREFIX + ":" + tenant + ":" + baseUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromFolioTenant(FolioTenant folioTenant) {
    return generateUUID(folioTenant.getTenant(), folioTenant.getBaseUrl());
  }

	// Method for removing the password from the tenant
	public static FolioTenant sanitiseFolioTenant(FolioTenant ten) {
		return FolioTenant.builder()
			.id(ten.id)
			.authType(ten.authType)
			.baseUrl(ten.baseUrl)
			.tenant(ten.tenant)
			.name(ten.name)
			.loginUser(ten.loginUser)
			.build();
	}

	// Method for adding the login password back to the tenant
	public static FolioTenant unsanitiseFolioTenant(FolioTenant ten, String password) {
		return FolioTenant.builder()
			.id(ten.id)
			.authType(ten.authType)
			.baseUrl(ten.baseUrl)
			.tenant(ten.tenant)
			.name(ten.name)
			.loginUser(ten.loginUser)
			.loginPassword(password)
			.build();
	}
}
