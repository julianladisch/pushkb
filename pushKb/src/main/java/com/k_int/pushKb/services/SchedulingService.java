package com.k_int.pushKb.services;


import java.time.Instant;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class SchedulingService {
	private final GoKBFeedService goKBFeedService;
	// FIXME This probably shouldn't be here
	private final SourceService sourceService;

	// FIXME remove this
	private final SourceRecordRepository sourceRecordRepository;

	public SchedulingService(
		GoKBFeedService goKBFeedService,
		SourceRecordRepository sourceRecordRepository,
		SourceService sourceService
	) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordRepository = sourceRecordRepository;
		this.sourceService = sourceService;
	}

  // FIXME need to work on delay here
  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		Mono.from(sourceService.findBySourceUrlAndCodeAndSourceType(
			"https://gokb.org/gokb/api",
			SourceCode.GOKB,
			SourceType.TIPP
		)).flatMap(this::handleSource)
			.subscribe();
	}

	public Mono<Instant> handleSource(Source source) {
		return Mono.from(sourceRecordRepository.findMaxLastUpdatedAtSourceBySource(source))
			// Is it the right thing to do here to use doOnSuccess?
			.doOnSuccess(maxVal -> {
				log.info("MAXIMUM TIMESTAMP FOUND: {}", maxVal);
				goKBFeedService.fetchGoKBTipps(source, Optional.ofNullable(maxVal));
			});
	}
}
