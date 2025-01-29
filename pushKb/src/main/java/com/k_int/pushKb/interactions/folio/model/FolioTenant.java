package com.k_int.pushKb.interactions.folio.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.k_int.pushKb.crud.HasId;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

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
@Builder(toBuilder = true)
public class FolioTenant implements HasId {
  @Id
	@TypeDef(type = DataType.UUID)
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

  private static final String UUID5_PREFIX = "folio_tenant";
  public static UUID generateUUID(String tenant, String baseUrl) {
    final String concat = UUID5_PREFIX + ":" + tenant + ":" + baseUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromFolioTenant(FolioTenant folioTenant) {
    return generateUUID(folioTenant.getTenant(), folioTenant.getBaseUrl());
  }
}
