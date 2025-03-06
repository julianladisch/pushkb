package com.k_int.pushKb.model;

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

  String getFilterContext();

  Instant getDestinationHeadPointer();
  void setDestinationHeadPointer( Instant pointer );

  Instant getLastSentPointer();
  void setLastSentPointer( Instant pointer );

  Instant getFootPointer();
  void setFootPointer( Instant pointer );
}
