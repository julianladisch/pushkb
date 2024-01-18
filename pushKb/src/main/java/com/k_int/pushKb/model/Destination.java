package com.k_int.pushKb.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;
import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;
import services.k_int.utils.UUIDUtils;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.micronaut.data.model.DataType;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class Destination {
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  DestinationType destinationType; // FOLIO etc

  // FIXME do we need a name for this?
/*   @NotNull
  @NonNull
  String name; // User defined name for this destination */

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String destinationUrl;

  // TODO Set<DestinationSourceLink> ??

  // Should be unique up to type/url
  private static final String UUID5_PREFIX = "destination";
  public static UUID generateUUID(DestinationType destinationType, String destinationUrl) {
    final String concat = UUID5_PREFIX + ":" + destinationType.toString() + ":" + destinationUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }
}
