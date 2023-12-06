package com.k_int.pushKb.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.model.SourceType;

import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;

@Singleton
public class SourceService {
  private final SourceRepository sourceRepository;
	public SourceService(
    SourceRepository sourceRepository
  ) {
    this.sourceRepository = sourceRepository;
	}

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<Source> ensureSource(String url, SourceCode code, SourceType type) {
    return sourceRepository.ensureSource(url, code, type);
  }

  // Must be protected at least to allow AOP annotations.
  // Adding this method gives us something to hang the transaction from. We also use the @Valid annotation
  // to validate the source record before we save it.
  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  protected Publisher<Source> saveSource ( @NonNull @Valid Source s ) {
  	return sourceRepository.save(s);
  }
}
