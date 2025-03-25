package com.k_int.pushKb.model;

import com.k_int.pushKb.transform.model.Transform;

import java.time.Instant;
import java.util.UUID;

// Covers both PushTask and TemporaryPushTask
public interface Pushable {
  UUID getId();

	UUID getPushableId();

	UUID getSourceId();
  Class<? extends Source> getSourceType();

  UUID getDestinationId();
  Class<? extends Destination> getDestinationType();

	UUID getTransformId();
	Class<? extends Transform> getTransformType();

  String getFilterContext();

  Instant getDestinationHeadPointer();
  void setDestinationHeadPointer( Instant pointer );

  Instant getLastSentPointer();
  void setLastSentPointer( Instant pointer );

  Instant getFootPointer();
  void setFootPointer( Instant pointer );
}
