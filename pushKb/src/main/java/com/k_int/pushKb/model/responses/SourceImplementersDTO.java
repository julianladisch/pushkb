package com.k_int.pushKb.model.responses;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object containing a list of available source implementer classes.
 * <p>
 * This DTO is typically used by discovery endpoints to inform clients about which
 * specific Java classes implement the {@code Source} interface, allowing for
 * dynamic configuration of data pull targets.
 */
@Data
@Builder
@Serdeable
@Schema(
	name = "SourceImplementers",
	description = "A wrapper object containing the class names of available Destination implementers."
)
public class SourceImplementersDTO {

	/**
	 * A list of fully qualified Java class names that implement the Source interface.
	 */
	@ArraySchema(
		schema = @Schema(
			description = "The fully qualified class name of a source implementer",
			example = "com.k_int.pushKb.interactions.gokb.model.GokbSource"
		)
	)
	List<String> implementers;
}
