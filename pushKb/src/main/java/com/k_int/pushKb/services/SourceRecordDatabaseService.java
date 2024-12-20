package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.ReadOnly;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Singleton
@Slf4j
public class SourceRecordDatabaseService {
  private final SourceRecordRepository sourceRecordRepository;

	public SourceRecordDatabaseService(
    SourceRecordRepository sourceRecordRepository
  ) {
    this.sourceRecordRepository = sourceRecordRepository;
	}

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<SourceRecord> saveOrUpdateRecord ( @NonNull @Valid SourceRecord sr ) {
    return sourceRecordRepository.saveOrUpdate(sr);
  }

  // Full count, not sure we'll need this v often
  @SingleResult
  @ReadOnly
  protected Publisher<Long> countRecords () {
    return sourceRecordRepository.count();
  }

  // THIS ASSUMES THAT SOURCES OF TWO DIFFERENT TYPES WILL NOT SHARE AN ID...
  // BE VERY CAREFUL WITH UUID5
  @ReadOnly
  protected Publisher<SourceRecord> getSourceRecordFeed (
    UUID sourceId,
    Instant footTimestamp,
    Instant headTimestamp,
    Optional<String> context
  ) {
    if (context.isPresent()) {
      return Flux.defer(() -> sourceRecordRepository.findTop1000BySourceIdAndFilterContextAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(sourceId, context.get(), footTimestamp, headTimestamp));
    }
  
    return Flux.defer(() -> sourceRecordRepository.findTop1000BySourceIdAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(sourceId, footTimestamp, headTimestamp));
  }

  @ReadOnly
  protected Publisher<SourceRecord> getSourceRecordFeedForUpdated (
    UUID sourceId,
    Instant updatedInstance,
    Optional<String> context
  ) {
    if (context.isPresent()) {
      return Flux.defer(() -> sourceRecordRepository.findBySourceIdAndFilterContextAndUpdatedOrderByUpdatedDescAndIdAsc(sourceId, context.get(), updatedInstance));
    }
  
    return Flux.defer(() -> sourceRecordRepository.findBySourceIdAndUpdatedOrderByUpdatedDescAndIdAsc(sourceId, updatedInstance));
  }

  @SingleResult
  @ReadOnly
  protected Publisher<Long> countFeed (UUID sourceId, Instant footTimestamp, Instant headTimestamp, Optional<String> context) {
    if (context.isPresent()) {
      return sourceRecordRepository.countBySourceIdAndFilterContextAndUpdatedGreaterThanAndUpdatedLessThan(sourceId, context.get(), footTimestamp, headTimestamp);
    }

    return sourceRecordRepository.countBySourceIdAndUpdatedGreaterThanAndUpdatedLessThan(sourceId, footTimestamp, headTimestamp);
  }

  @SingleResult
  @ReadOnly
  public Publisher<Instant> findTopOfFeed (UUID sourceId, Optional<String> context) {
    if (context.isPresent()) {
      return sourceRecordRepository.findMaxUpdatedBySourceIdAndFilterContext(sourceId, context.get());
    }

    return sourceRecordRepository.findMaxUpdatedBySourceId(sourceId);
  }
}
