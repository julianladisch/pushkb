package com.k_int.pushKb.core.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.data.annotation.MappedEntity;

import io.micronaut.serde.annotation.Serdeable;


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
public class HelloWorld extends Generic {
	@Nullable
  private String test1;

  @Nullable
  private String test2;
}