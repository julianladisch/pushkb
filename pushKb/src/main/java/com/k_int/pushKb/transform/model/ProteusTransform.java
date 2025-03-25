package com.k_int.pushKb.transform.model;


import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import io.micronaut.core.annotation.Nullable;

import java.util.UUID;

@Data
@Serdeable
@Builder
public class ProteusTransform implements Transform {
	@NotNull
	@NonNull
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

	private final String name;
	private final String slug;

	private final ProteusSpecSource source;

	@Nullable
	private final String specFile; // Holds file name if source is file

	@Nullable
	private final JsonNode spec; // Holds spec itself if source is spec
}
