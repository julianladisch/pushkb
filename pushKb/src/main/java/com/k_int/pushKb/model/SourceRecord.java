package com.k_int.pushKb.model;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;

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

	@TypeDef(type = DataType.STRING)
  SourceCode source;

	@TypeDef(type = DataType.STRING)
  SourceRecordType recordType;

  Instant timestamp;

  JsonNode jsonRecord;
}
