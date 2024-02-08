package com.k_int.pushKb.destinations.folio;

import java.util.UUID;

import com.k_int.pushKb.model.Error;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.AutoPopulated;
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

@Serdeable
@Data
@MappedEntity("folio_destination_error")
@AllArgsConstructor
@Builder(toBuilder = true)
public class FolioDestinationError implements Error<FolioDestination> {
  static final String LOGIN_ERROR_CODE = "LOGIN_ERROR";
  static final String CONNECT_ERROR_CODE = "CONNECT_ERROR";

  @Id
	@TypeDef(type = DataType.UUID)
  @AutoPopulated
	private UUID id;

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  protected String code;

  @NotNull
  @NonNull
  @Size(max = 255)
  protected String message;

  @NotNull
  @NonNull
  FolioDestination owner; // Hangs off a FolioDestination
}
