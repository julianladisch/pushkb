package com.k_int.pushKb.interactions.folio.api;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Destinations: FolioDestination/FolioTenant", description="A FolioTenant represents a FOLIO tenant configuration " +
	"that can be associated with FolioDestinations to which data can be pushed along the \"/erm/pushKB/*\" endpoints. These can be " +
	"created and managed individually through these endpoints, or created implicitly via a FolioDestination POST. Updates to FolioTenant " +
	"need to be managed through the PUT endpoints here, however")
public interface FolioTenantApi {

}
