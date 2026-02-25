package com.k_int.pushKb.interactions.gokb.api;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Sources: GokbSource/Gokb", description="A Gokb contains shared information about the Gokb instance to which " +
	"1 or more GokbSources can be appended, representing the more granular sources for TIPPs etc.  These can be created " +
	"and managed individually through these endpoints, or created implicitly via a Gokb POST. " +
	"Updates to Gokb need to be managed through the PUT endpoints here, however")
public interface GokbApi {
}
