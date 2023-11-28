package com.k_int.pushKb.model;


import java.util.ArrayList;
import java.util.UUID;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Serdeable
@NoArgsConstructor(onConstructor_ = @Creator())
@AllArgsConstructor
@MappedEntity
@ToString
public class GoKBScrollAPIPage {
	//@NotNull
	//@NonNull
	@Nullable
  @AutoPopulated
	@Id
	@TypeDef( type = DataType.UUID)
	private UUID id;

	@Nullable
  private String result;

  @Nullable
  private Integer scrollSize;

  @Nullable
  private Integer lastPage;

  @Nullable
  private String scrollId;

  @Nullable
  private Boolean hasMoreRecords;

  @Nullable
  private Integer total;

  @Nullable
  // FIXME not working
  @JsonDeserialize(as = GoKBRecordDeserializer.class)
  private ArrayList<String> records; // BIIIIG json blob?

  @Nullable
  private Integer size;
}
