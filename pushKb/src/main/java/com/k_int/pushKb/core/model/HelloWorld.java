package com.k_int.pushKb.core.model;

import java.util.UUID;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor(onConstructor_ = @Creator())
@AllArgsConstructor
@Serdeable
@MappedEntity
@ToString
public class HelloWorld {
	// FIXME This cannot be right
	//@NotNull
	//@NonNull
	@Id
	@TypeDef( type = DataType.UUID)
	private UUID id;

	@Nullable
  private String test1;

  @Nullable
  private String test2;
}