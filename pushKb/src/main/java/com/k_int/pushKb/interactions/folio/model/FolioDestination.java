package com.k_int.pushKb.interactions.folio.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.k_int.pushKb.model.Destination;

import io.micronaut.core.annotation.NonNull;
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

@Serdeable
@Data
@MappedEntity("folio_destination")
@AllArgsConstructor
@Builder(toBuilder = true)
public class FolioDestination implements Destination {
  @Id
	@TypeDef(type = DataType.UUID)
	private UUID id;
    // FIXME do we need a name for this?
/*   @NotNull
  @NonNull
  String name; // User defined name for this destination */

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  protected String destinationUrl;

  private final String tenant;

  @NotNull
  @NonNull
  @Size(max = 200)
  private final String loginUser;
  @NotNull
  @NonNull
  @Size(max = 200)
  private final String loginPassword;

  private static final String UUID5_PREFIX = "folio_destination";
  public static UUID generateUUID(String tenant, String destinationUrl) {
    final String concat = UUID5_PREFIX + ":" + tenant + ":" + destinationUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromDestination(FolioDestination destination) {
    return generateUUID(destination.getTenant(), destination.getDestinationUrl());
  }
}