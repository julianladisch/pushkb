package com.k_int.pushKb.interactions.gokb.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.util.UUID;

import com.k_int.pushKb.model.Source;

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
import io.micronaut.data.model.DataType;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity("gokb_source")
@Builder(toBuilder = true)
public class GokbSource implements Source {
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  GokbSourceType gokbSourceType; // Package vs TIPP

  @NotNull
  @NonNull
  Gokb gokb;
  
  // Scrolling api available here
  @Transient
  public String getSourceUrl() {
    // GokbSource will be gokb url + `gokb/api/`
    // ASSUMES that Gokb URL is of the form <gokb domain> (no trailing slash, domain ONLY)
    // Trailing slash needed for this to be treated as baseUrl for relative.
    // If we want to support OAIPMH in future that will need a different path (/gokb/oai/index/)...
    // Either different source or switch in class somewhere
    return gokb.getBaseUrl() + "/gokb/api/";
  }

  // Should be unique up to type/gokb
  private static final String UUID5_PREFIX = "gokb_source";
  public static UUID generateUUID(Gokb gokb, GokbSourceType gokbSourceType) {
    final String concat = UUID5_PREFIX + ":" + ":" + Gokb.generateUUIDFromGoKB(gokb).toString() + ":" + gokbSourceType.toString();
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromSource(GokbSource source) {
    return generateUUID(source.getGokb(), source.getGokbSourceType());
  }
}
