package com.k_int.pushKb.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SourceRecordService {
  private final SourceRecordRepository sourceRecordRepository;

	public SourceRecordService(
    SourceRecordRepository sourceRecordRepository
  ) {
    this.sourceRecordRepository = sourceRecordRepository;
	}

  // Must be protected at least to allow AOP annotations.
  // Adding this method gives us something to hang the transaction from. We also use the @Valid annotation
  // to validate the source record before we save it.
  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  protected Publisher<SourceRecord> saveRecord ( @NonNull @Valid SourceRecord sr ) {
  	return sourceRecordRepository.save(sr);
  }
}
