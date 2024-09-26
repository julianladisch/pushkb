package com.k_int.pushKb.services;

//import java.time.Instant;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
@Singleton
@Slf4j
public class SchedulingService {
	private final SourceService sourceService;
	private final PushTaskDatabaseService pushTaskDatabaseService;
	private final PushService pushService;

	// TODO Are we using this in the end?
	private final DestinationService destinationService;

	// These are here to make sure that if this is running an ingest or a push
	// we don't try to trigger another one, eg if the scheduler refires before finishing
	private Disposable pushTaskRunnerDisposable;
	private Disposable ingestRunnerDisposable;

	public SchedulingService(
		SourceService sourceService,
		PushTaskDatabaseService pushTaskDatabaseService,
		PushService pushService,
		DestinationService destinationService
	) {
		this.sourceService = sourceService;
		this.pushTaskDatabaseService = pushTaskDatabaseService;
		this.pushService = pushService;
		this.destinationService = destinationService;
	}

	public void resetPushRunner() {
		pushTaskRunnerDisposable = null;
	}

	public void resetIngestRunner() {
		ingestRunnerDisposable = null;
	}

	// Run 30 mins out of sync with ingest -- idea is that it shouldn't care if things are already running etc.
	// THIS WILL NOT WORK RN UNTIL GOKB FIX EXPECTED 10th OCT 2024
	/* @Scheduled(initialDelay = "30m", fixedDelay = "1h")
	public void pushRunner() {
		if (pushTaskRunnerDisposable == null) {
			// Iterate over all PushTasks, maybe want to be smarter about this in future
			pushTaskRunnerDisposable = Flux.from(pushTaskDatabaseService.getPushTaskFeed())
				.concatMap(pushService::runPushTask)
				.subscribe(
					pt -> log.info("PushTask({}) completed at {}", pt.getId(), Instant.now()), // Consumer (doOnNext)
					e -> {
						log.error("Something went wrong in pushTaskScheduler: {}", e);
						resetPushRunner();
					}, // errorConsumer (doOnError)
					() -> resetPushRunner() // completeConsumer (doOnComplete)
				);
		} else {
			log.warn("Pushes in progress, skipping");
		}
	} */

  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void ingestRunner() {
		if (ingestRunnerDisposable == null) {
			// Fetch all source implementers from sourceService
			ingestRunnerDisposable = Flux.from(sourceService.getSourceImplementors())
				// For each class implementing Source, list all actual Sources in DB
				// PushTasks may rely on whether these have completed or not...
				.flatMap(sourceService::list)
				// For each source, trigger an ingest of all records
				.concatMap(sourceService::triggerIngestForSource) // OUTPUT is final saved source.
				.subscribe(
					sr -> {}, // Consumer (doOnNext)
					e -> {
						log.error("Something went wrong in ingestScheduler: {}", e);
						resetIngestRunner();
					}, // errorConsumer (doOnError)
					() -> resetIngestRunner() // completeConsumer (doOnComplete)
				);
		} else {
			log.warn("Ingest in progress, skipping");
		}
	}
}
