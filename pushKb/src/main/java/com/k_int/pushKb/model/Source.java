package com.k_int.pushKb.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
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
public class Source {
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  SourceCode code; // GOKB etc

  @NotNull
  @NonNull
  SourceType sourceType; // Package vs TIPP

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String sourceUrl;

  // Should be unique up to code/type/url
  private static final String UUID5_PREFIX = "source";
  public static UUID generateUUID(SourceCode code, SourceType sourceType, String sourceUrl) {
    final String concat = UUID5_PREFIX + ":" + code.toString() + ":" + sourceType.toString() + ":" + sourceUrl;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromSource(Source source) {
    return generateUUID(source.getCode(), source.getSourceType(), source.getSourceUrl());
  }  
}
