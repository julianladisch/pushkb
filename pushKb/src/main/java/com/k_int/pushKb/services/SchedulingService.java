package com.k_int.pushKb.services;

import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class SchedulingService {
	private final GoKBFeedService goKBFeedService;

	// FIXME remove this
	private final SourceRecordRepository sourceRecordRepository;

	public SchedulingService(GoKBFeedService goKBFeedService, SourceRecordRepository sourceRecordRepository) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordRepository = sourceRecordRepository;
	}

  // FIXME need to work on delay here
  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		Mono.from(sourceRecordRepository.count())
				.doOnNext(count -> log.info("COUNT OF SOURCE RECORDS: {}", count))
				.doOnNext(count -> {
					if (count == 0) {
						log.info("There are no records in place, fetching");
						goKBFeedService.fetchGoKBTipps();
					} else {
						log.info("There are records in place, skipping");
					}
				})
				.subscribe();
	}
}
