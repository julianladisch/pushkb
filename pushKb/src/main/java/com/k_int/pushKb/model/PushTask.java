package com.k_int.pushKb.model;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

import java.time.Instant;
import java.util.UUID;

import com.k_int.pushKb.converters.ClassAttributeConverter;
import com.k_int.pushKb.crud.HasId;
import com.k_int.pushKb.serde.ClassSerde;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.annotation.Serdeable.Deserializable;
import io.micronaut.serde.annotation.Serdeable.Serializable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import services.k_int.utils.UUIDUtils;

// Class to model the actual destination -> source tasks that will need to happen
@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class PushTask implements Pushable, HasId {
  @Id
	@TypeDef(type = DataType.UUID)
	private UUID id;

  // TODO we will eventually need to model transform "properly"
  @NotNull
  @NonNull
  private String transform;

  private UUID sourceId;
  @TypeDef(type = DataType.STRING, converter = ClassAttributeConverter.class)
  @Serializable(using=ClassSerde.class)
  @Deserializable(using=ClassSerde.class)
  private Class<? extends Source> sourceType;

  private UUID destinationId;
  @TypeDef(type = DataType.STRING, converter = ClassAttributeConverter.class)
  @Serializable(using=ClassSerde.class)
  @Deserializable(using=ClassSerde.class)
  private Class<? extends Destination> destinationType;

  /* This record will hold pointers indicating which source_records have been successfully sent.
   * The algorithm will iterate backwards through these, so we need 3 pointers
   * (and pointer from head of source_record stack) in order to keep track of the "gap"
   * that forms while sending records in chunks.
   */
  @Builder.Default
  Instant destinationHeadPointer = Instant.EPOCH; // Tracks the "head" from the destination's point of view
  @Builder.Default
  Instant lastSentPointer = Instant.EPOCH; // Tracks the last successfully sent instant
  @Builder.Default
  Instant footPointer = Instant.EPOCH; // Tracks the point beyond which ALL records have been sent

	@Transient
	public UUID getPushableId() {
		return getId();
	}

    // Generate id from destination/source (Should be unique to those 2)
  private static final String UUID5_PREFIX = "push_task";
  public static UUID generateUUID(UUID sourceId, UUID destinationId) {
    final String concat = UUID5_PREFIX + ":" + sourceId.toString() + ":" + destinationId.toString();
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
  }

  public static UUID generateUUIDFromPushTask(PushTask pt) {
    return generateUUID(pt.getSourceId(), pt.getDestinationId());
  }

	public void resetPointer() {
		this.destinationHeadPointer = Instant.EPOCH;
		this.lastSentPointer = Instant.EPOCH;
		this.footPointer = Instant.EPOCH;
	}


  // FIXME is there a better way to ensure PushTask and TemporaryPushTask both surface a filterContext?
  @Transient
  public String getFilterContext() {
    return null;
  }
}

  /*
   * -- POINTER EXAMPLE --
   *
   * Record 13 * head (implicit, can be fetched from DB)
   * Record 12
   * Record 11
   * Record 10 * destinationHeadPointer
   * Record 9
   * Record 8
   * Record 7 * lastSentPointer
   * Record 6
   * Record 5
   * Record 4
   * Record 3 * footPointer
   * Record 2
   * Record 1
   *
   * would mean that the destination has successfully recieved records 1-3, and also 7-10.
   * We need to iterate through 6-4, then move up to record 13.
   *
   * This *should* only look like this mid-process, but this tracking
   * will also allow us to cope with failed pushes.
   *
   * NOTE pointers actually point at "updated" timestamp for those records right now,
   * not the records themselves. This may need thought. On the one hand if there is a
   * duplicated timestamp we could have an issue. On the other hand if we point at a record
   * and that record gets updated, the pointer has jump in the queue, which is not acceptable
   */
