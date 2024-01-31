package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;

@Singleton
public class SourceRecordService {
  private final SourceRecordRepository sourceRecordRepository;

	public SourceRecordService(
    SourceRecordRepository sourceRecordRepository
  ) {
    this.sourceRecordRepository = sourceRecordRepository;
	}

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<SourceRecord> saveOrUpdateRecord ( @NonNull @Valid SourceRecord sr ) {
  	return sourceRecordRepository.saveOrUpdate(sr);
  }

  @Transactional
  @SingleResult
  protected Publisher<Long> countRecords () {
    return sourceRecordRepository.count();
  }

  @Transactional
  protected Publisher<SourceRecord> getSourceRecordFeedBySource (Source source, Instant footTimestamp, Instant headTimestamp) {
    return getSourceRecordFeedBySourceId(source.getId(), footTimestamp, headTimestamp);
  }

  // THIS ASSUMES THAT SOURCES OF TWO DIFFERENT TYPES WILL NOT SHARE AN ID...
  // BE VERY CAREFUL WITH UUID5
  @Transactional
  protected Publisher<SourceRecord> getSourceRecordFeedBySourceId (UUID sourceId, Instant footTimestamp, Instant headTimestamp) {
    return sourceRecordRepository.findAllBySourceIdAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(sourceId, footTimestamp, headTimestamp);
  }

  @Transactional
  @SingleResult
  public Publisher<Instant> findMaxLastUpdatedAtSourceBySource (Source source) {
    return sourceRecordRepository.findMaxLastUpdatedAtSourceBySourceId(source.getId());
  }
  // FIXME should probably change these methods to use UUID as well? Maybe not, see where the wind takes you

  @Transactional
  @SingleResult
  public Publisher<Instant> findMaxUpdatedBySource (Source source) {
    return findMaxUpdatedBySourceId(source.getId());
  }

  @Transactional
  @SingleResult
  public Publisher<Instant> findMaxUpdatedBySourceId (UUID sourceId) {
    return sourceRecordRepository.findMaxUpdatedBySourceId(sourceId);
  }
}
