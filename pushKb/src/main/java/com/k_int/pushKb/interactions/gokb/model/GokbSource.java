package com.k_int.pushKb.interactions.gokb.model;

import static com.k_int.pushKb.Constants.UUIDs.*;

import java.time.Instant;
import java.util.UUID;

import com.k_int.pushKb.crud.HasId;
import com.k_int.pushKb.model.Source;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import services.k_int.utils.UUIDUtils;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity("gokb_source")
@Builder(toBuilder = true)
@Introspected // Do we actually want to be doing this to allow us to validate Gokbs as part of sources?
public class GokbSource implements Source, HasId {
	@Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  @NotNull
  @NonNull
  GokbSourceType gokbSourceType; // Package vs TIPP

  @NotNull
  @NonNull
  @Valid // Needs @Introspected
  Gokb gokb;

  @NotNull
  @NonNull
  @ToString.Include
	@Size(max = 200)
  String name;

  @Nullable
  Instant pointer;

  @Nullable
  Instant lastIngestStarted;

  @Nullable
  Instant lastIngestCompleted;

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
