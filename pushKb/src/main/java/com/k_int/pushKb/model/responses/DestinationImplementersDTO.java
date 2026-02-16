package com.k_int.pushKb.model.responses;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object containing a list of available destination implementer classes.
 * <p>
 * This DTO is typically used by discovery endpoints to inform clients about which
 * specific Java classes implement the {@code Destination} interface, allowing for
 * dynamic configuration of data push targets.
 */
@Data
@Builder
@Serdeable
@Schema(
	name = "DestinationImplementers",
	description = "A wrapper object containing the class names of available Destination implementers."
)
public class DestinationImplementersDTO {

	/**
	 * A list of fully qualified Java class names that implement the Destination interface.
	 */
	@ArraySchema(
		schema = @Schema(
			description = "The fully qualified class name of a destination implementer",
			example = "com.k_int.pushKb.interactions.folio.model.FolioDestination"
		)
	)
	List<String> implementers;
}
