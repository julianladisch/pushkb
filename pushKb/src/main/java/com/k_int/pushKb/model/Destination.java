package com.k_int.pushKb.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
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
@Builder
public class Destination {
  @AutoPopulated
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  DestinationType destinationType; // FOLIO etc

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String destinationUrl;

  // TODO Set<DestinationSourceLink> 
}
