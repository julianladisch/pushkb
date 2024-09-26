package com.k_int.pushKb.services;

import com.k_int.pushKb.model.Source;

import reactor.core.publisher.Mono;

// Use generic here for ability to fetch the feed service later (??)
public interface SourceFeedService<T extends Source> {
  Mono<T> fetchSourceRecords(T source);
}
