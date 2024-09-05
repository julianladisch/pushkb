package com.k_int.pushKb.services;

import java.time.Instant;

import com.k_int.pushKb.Boostraps;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.model.Source;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Singleton
@Slf4j
public class SchedulingService {
	private final SourceService sourceService;
	private final PushTaskDatabaseService pushTaskDatabaseService;
	private final PushService pushService;

	// TODO Are we using this in the end?
	private final DestinationService destinationService;

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

/* 	@Scheduled(initialDelay = "30s", fixedDelay = "1h")
	public void pushRunner() {
		if (pushTaskRunnerDisposable == null) {
			// Iterate over all PushTasks, maybe want to be smarter about this in future
			pushTaskRunnerDisposable = Flux.from(pushTaskDatabaseService.getPushTaskFeed())
				.concatMap(pushService::runPushTask)
				.subscribe(
					pt -> log.info("PushTask({}) completed at {}", pt, Instant.now()), // Consumer (doOnNext)
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

  // FIXME need to work on delay here
  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void ingestRunner() {
		if (ingestRunnerDisposable == null) {
			// Fetch all source implementers from sourceService
			ingestRunnerDisposable = Flux.from(sourceService.getSourceImplementors())
				// For each class implementing Source, list all actual Sources in DB
				// PushTasks may rely on whether these have completed or not...
				.flatMap(sourceService::list)
				// For each source, trigger an ingest of all records
				.concatMap(sourceService::triggerIngestForSource)
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

	// FETCHING FROM FOLIO---?
/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		// For now, grab our FolioDestination from Bootstraps directly
		Mono.from(destinationService.findById(
			FolioDestination.class,
			//FolioDestination.generateUUIDFromDestination(
			//	(FolioDestination) Boostraps.destinations.get("LOCAL_RANCHER_FOLIO")
			//)
			//FolioDestination.generateUUIDFromDestination(
			//	(FolioDestination) Boostraps.destinations.get("SNAPSHOT")
			//)
			FolioDestination.generateUUIDFromDestination(
				(FolioDestination) Boostraps.destinations.get("LOCAL_DC_FOLIO")
			)
			//FolioDestination.generateUUIDFromDestination(
			//	(FolioDestination) Boostraps.destinations.get("SNAPSHOT")
			//)
		))
			.flatMapMany(destinationService::testMethod)
			.subscribe();
	} */
}
