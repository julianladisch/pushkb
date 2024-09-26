package com.k_int.pushKb.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// A domain class to group all the API calls out during a pushTask
//Starting out it'll just be an id, but it gives us a place to hang things off
// like logging or metadata information
@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class PushSession {
  @Id
	@TypeDef(type = DataType.UUID)
  @AutoPopulated // USING auto population instead of UUID5 for this class to avoid any duplications.
  @NotNull
  @NonNull
	private UUID id;

  @NotNull
  @NonNull
  PushTask pushTask;

  @NotNull
  @NonNull
  @DateCreated
  Instant created;
  
  @Relation(
    value = Relation.Kind.ONE_TO_MANY,
    mappedBy = "session"
  )
  Set<PushChunk> chunks;
}
