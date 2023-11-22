package com.k_int.pushKb.core.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor(onConstructor_ = @Creator())
@AllArgsConstructor
@MappedEntity
@ToString
public abstract class Generic {
	@NotNull
	@NonNull
	@Id
	@TypeDef( type = DataType.UUID)
	private UUID id;
}