package com.k_int.pushKb.model;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Serdeable
public class DestinationImplementersDTO {
	@Schema(example = "[\"class com.k_int.pushKb.interactions.folio.model.FolioDestination\"]")
	List<String> implementers;
}
