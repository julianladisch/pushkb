package com.k_int.pushKb.services;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

@Singleton
public class SchedulingService {
	private final GoKBFeedService goKBFeedService;

	public SchedulingService(GoKBFeedService goKBFeedService) {
		this.goKBFeedService = goKBFeedService;
	}

  // FIXME need to work on delay here
  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
    goKBFeedService.testScheduling();
	}
}
