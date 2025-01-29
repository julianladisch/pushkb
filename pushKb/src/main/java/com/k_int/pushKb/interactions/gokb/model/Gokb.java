package com.k_int.pushKb.interactions.gokb.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.k_int.pushKb.crud.HasId;

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

/*
 * An object to store all gokb base interaction details,
 * so we don't need repeats for each gokb in the system
 * (But leaves source as the granular object for sourceRecord/pushTask)
 */
@Serdeable
@Data
@AllArgsConstructor
@MappedEntity("gokb")
@Builder(toBuilder = true)
public class Gokb implements HasId { // FIXME not a huge fan of this HasId interface, would prefer an annotation but idk how to do that
  @Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  // We assume that this needs to be of the form <gokb domain> (no trailing slash or /gokb...) for now.
  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String baseUrl;

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String name;

  // Should be unique up to url
  private static final String UUID5_PREFIX = "gokb_object";
  public static UUID generateUUID(String baseUrl) {
    final String concat = UUID5_PREFIX + ":" + ":" + baseUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromGoKB(Gokb gokb) {
    return generateUUID(gokb.getBaseUrl());
  }
}
