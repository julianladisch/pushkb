package com.k_int.pushKb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.util.UUID;
import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;
import services.k_int.utils.UUIDUtils;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;



@Serdeable
@Data
@AllArgsConstructor
@MappedEntity
@Builder(toBuilder = true)
public class DestinationSourceLink {
  @Id
	@TypeDef(type = DataType.UUID)
  @NotNull
  @NonNull
	private UUID id;


  /* This won't be a string.
   * Strategy will be to find all destination source links by transform and source, then we can send to multiple destinations while only transforming each chunk once
   */
  @NotNull
  @NonNull
  String transform;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  @NotNull
  @NonNull
  Destination destination;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  @NotNull
  @NonNull
  Source source;

  /* This record will hold pointers indicating which source_records have been successfully sent.
   * The algorithm will iterate backwards through these, so we need 3 pointers
   * (and pointer from head of source_record stack) in order to keep track of the "gap"
   * that forms while sending records in chunks.
   */
  Instant destinationHeadPointer; // Tracks the "head" from the destination's point of view
  Instant lastSentPointer; // Tracks the last successfully sent instant
  Instant footPointer; // Tracks the point beyond which ALL records have been sent
 

  // Generate id from destination/source/transform (Should be unique to those 3)
  private static final String UUID5_PREFIX = "destination_source_link";
  public static UUID generateUUID(Destination destination, Source source, String transform) {
    final String concat = UUID5_PREFIX + ":" + destination.toString() + ":" + source.toString() + ":" + transform;
    return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
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
   */