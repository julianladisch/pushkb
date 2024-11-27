package com.k_int.pushKb.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.data.annotation.Transient;

public interface Source {
  static Logger log = LoggerFactory.getLogger(Source.class);

  // USE UUID5 FOR SOURCE IDs, ENSURE THAT TWO SOURCES CANNOT SHARE AN ID
  public UUID getId();

  // Name is really only useful for connecting these things together later
  public String getName();

  // A source currently must have a URL
  public String getSourceUrl();

  // Pointer for feeds to work from
  // IMPORTANT -- because this is updated throughout the feed it is maintained by the sourceFeedService
  public Instant getPointer();

  // Track when last ingest started and finished so we have a database
  // way to know whether an ingest is running (or died/errored)

  // As of right now we _won't_ use this to continue/ignore ingests
  // but we _will_ use it to ignore a PushTask in certain circumstances (PushTaskPair)
  // These will be maintained by the sourceService so there isn't work required within each sourceFeedService

  public Instant getLastIngestStarted();
  public void setLastIngestStarted(Instant started);

  public Instant getLastIngestCompleted();
  public void setLastIngestCompleted(Instant started);

  // Default method to get the duration of the last ingest.
  // Returns null if ingest is running/has never run.
  @Transient // This will be a transient field on all implementations -- this seems to work?
  default Duration getLastIngestDuration() {
    if (getLastIngestStarted() == null) {
      log.warn("No ingest has ever run for source({})", getId());
      return null;
    }

    if (getLastIngestCompleted() == null) {
      log.warn("No ingest has ever completed for source({})", getId());
      return null;
    }

    Duration duration = Duration.between(getLastIngestStarted(), getLastIngestCompleted());
    if (duration.toMillis() < 0) {
      log.warn("Last ingest is still running or did not complete for source({})", getId());
      return null;
    }
    return duration;
  }
}
