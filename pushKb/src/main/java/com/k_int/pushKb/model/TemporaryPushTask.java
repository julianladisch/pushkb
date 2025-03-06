package com.k_int.pushKb.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;

import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import services.k_int.utils.UUIDUtils;

@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class TemporaryPushTask implements Pushable {
  @Id
	@TypeDef(type = DataType.UUID)
  @NotNull
  @NonNull
	private UUID id;

  // Temporary pushTasks can ONLY be created from a base pushTask.
  // It's effectively a "do over" either for a filtered subset or the entire run.
  @NonNull
  @NotNull
  PushTask pushTask;

  // Can make use of the filterContext on sourceRecords to filter those
  @Nullable
  private String filterContext;

  /* See regular PushTask class for details on how this works
   * TODO consider "PushTaskPointer" object to capture same shape in Temp and regular PushTask
   */
  @Builder.Default
  Instant destinationHeadPointer = Instant.EPOCH; // Tracks the "head" from the destination's point of view
  @Builder.Default
  Instant lastSentPointer = Instant.EPOCH; // Tracks the last successfully sent instant
  @Builder.Default
  Instant footPointer = Instant.EPOCH; // Tracks the point beyond which ALL records have been sent


  // Pass through methods needed for Pushable interface dep
  @Transient
  public UUID getSourceId() {
    return getPushTask().getSourceId();
  }

	@Transient
	public UUID getPushableId() {
		return getPushTask().getId();
	}

  @Transient
  public Class<? extends Source> getSourceType() {
    return getPushTask().getSourceType();
  }

  @Transient
  public UUID getDestinationId() {
    return getPushTask().getDestinationId();
  }

  @Transient
  public Class<? extends Destination> getDestinationType() {
    return getPushTask().getDestinationType();
  }

    // Generate id from destination/source (Should be unique to those 2)
  private static final String UUID5_PREFIX = "temporary_push_task";
  public static UUID generateUUID(PushTask pushTask, String filterContext) {
    final String fc = Objects.requireNonNullElse(filterContext, "null");
    
    final String concat = UUID5_PREFIX + ":" + pushTask.toString() + ":" + fc;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromTemporaryPushTask(TemporaryPushTask tpt) {
    return generateUUID(tpt.getPushTask(), tpt.getFilterContext());
  }
}
