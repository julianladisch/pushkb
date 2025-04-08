package com.k_int.pushKb.transform.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// TODO We use THIS to ensure that NO changes can happen to the slug
@Data
@Serdeable
@Builder
public class ProteusTransformDTO {
	private final UUID id;

	private final String name;

	private final ProteusSpecSource source;

	@Nullable
	private final String specFile; // Holds file name if source is file

	@Nullable
	private final JsonNode spec; // Holds spec itself if source is spec
}

