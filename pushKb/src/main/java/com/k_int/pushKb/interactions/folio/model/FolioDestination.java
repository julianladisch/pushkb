package com.k_int.pushKb.interactions.folio.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.k_int.pushKb.crud.HasId;
import com.k_int.pushKb.model.Destination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import services.k_int.utils.UUIDUtils;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.micronaut.data.model.DataType;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity("folio_destination")
@Builder(toBuilder = true)
public class FolioDestination implements Destination, HasId {
  @Id
	@TypeDef(type = DataType.UUID)
	@Schema(
		description = "The unique identifier, automatically generated from the tenant and type.",
		accessMode = Schema.AccessMode.READ_ONLY // This hides it from the "Request Body" in Swagger
	)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY) // This prevents the JSON parser from accepting it on input
	private UUID id;

  @NotNull
  @NonNull
  FolioDestinationType destinationType;

  @NotNull
  @NonNull
	@Schema(
		name = "FolioTenant", // Explicitly names the sub-object
		description = "The tenant configuration this destination will push to."
	)
  FolioTenant folioTenant;

  @NotNull
  @NonNull
  @Size(max = 200)
  String name;
  
  // FIXME work out whether url should be here as part of destination itself... GOKB only goes to "/golb/api" level so maybe not
  @Transient
  public String getDestinationUrl() {
    /*
     * FolioDestination url is folioTenant baseUrl.
     * To mirror gokbSource pattern we won't append (say) /erm/pushKB/pushPCI here,
     * instead keeping that in the client. I'm not 100% certain on this pattern atm
     */
    return folioTenant.getBaseUrl() ;
  }

  private static final String UUID5_PREFIX = "folio_destination";
  public static UUID generateUUID(FolioTenant folioTenant, FolioDestinationType destinationType) {
    final String concat = UUID5_PREFIX + ":" + FolioTenant.generateUUIDFromFolioTenant(folioTenant) + ":" + destinationType.toString();
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromDestination(FolioDestination destination) {
    return generateUUID(destination.getFolioTenant(), destination.getDestinationType());
  }
}
