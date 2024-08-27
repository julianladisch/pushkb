package com.k_int.pushKb.model;

import java.time.Instant;
import java.util.UUID;

import com.k_int.pushKb.converters.ClassAttributeConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;
import services.k_int.utils.UUIDUtils;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import io.micronaut.data.model.DataType;
import io.micronaut.json.tree.JsonNode;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class SourceRecord {
	@Id
	@TypeDef(type = DataType.UUID)
  @NotNull
  @NonNull
	private UUID id;

  // The UUID of the record on the source
	String sourceUUID;

  // Link to the source in the DB (implementor of source)
  @NotNull
  @NonNull
  UUID sourceId;
  @TypeDef(type = DataType.STRING, converter = ClassAttributeConverter.class)
  Class<? extends Source> sourceType;

  @DateCreated
  Instant created;
  
  @DateUpdated
  Instant updated;
  
  Instant lastUpdatedAtSource;

  @TypeDef(type = DataType.JSON)
  @NotNull
  @NonNull
  JsonNode jsonRecord;

  private static final String UUID5_PREFIX = "source_record";
  public static UUID generateUUID(Class<? extends Source> sourceType, UUID sourceId, String sourceUUID) {
    final String concat = UUID5_PREFIX + ":" + sourceType.getName() + ":" + sourceId.toString() + ":" + sourceUUID;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromSourceRecord(SourceRecord sr) {
    return generateUUID(sr.getSourceType(), sr.getSourceId(), sr.getSourceUUID());
  }
}
