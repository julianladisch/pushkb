package com.k_int.pushKb.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.AutoPopulated;
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
@Builder
public class SourceRecord {
  @AutoPopulated
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  SourceCode source;

  @NotNull
  @NonNull
  SourceRecordType recordType;

  @DateCreated
  Instant created;
  
  @DateUpdated
  Instant updated;

  @TypeDef(type = DataType.JSON)
  @NotNull
  @NonNull
  JsonNode jsonRecord;
}
