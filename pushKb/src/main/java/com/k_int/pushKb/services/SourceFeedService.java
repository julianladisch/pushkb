package com.k_int.pushKb.services;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;

import reactor.core.publisher.Flux;

// Use generic here for ability to fetch the feed service later (??)
public interface SourceFeedService<T extends Source> {
  Flux<SourceRecord> fetchSourceRecords(T source);
}
