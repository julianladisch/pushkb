package com.k_int.pushKb.services;

import com.k_int.pushKb.Boostraps;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.model.Source;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Singleton
@Slf4j
public class SchedulingService {
	private final SourceService sourceService;
	private final PushTaskService pushTaskService;
	private final PushService pushService;

	// TODO Are we using this in the end?
	private final DestinationService destinationService;

	public SchedulingService(
		SourceService sourceService,
		PushTaskService pushTaskService,
		PushService pushService,
		DestinationService destinationService
	) {
		this.sourceService = sourceService;
		this.pushTaskService = pushTaskService;
		this.pushService = pushService;
		this.destinationService = destinationService;
	}

/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testSendAlgorithm() {
		log.info("TESTING PUSH ALGORITHM");
			// Iterate over all PushTasks, maybe want to be smarter about this in future
			Flux.from(pushTaskService.getPushTaskFeed())
				.flatMap(pushService::runPushTask)
				.doOnNext(pt -> log.info("WHEN DO WE SEE THIS FINAL END? {}", pt))
				.subscribe();
	} */

  // FIXME need to work on delay here
/*   @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
			// Fetch all source implementers from sourceService
			Flux.from(sourceService.getSourceImplementors())
			// For each class implementing Source, list all actual Sources in DB
			.flatMap(sourceService::list)
			// For each source, trigger an ingest of all records
			.flatMap(sourceService::triggerIngestForSource)
			.subscribe();
	} */

	// FETCHING FROM FOLIO---?
	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
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
		))
			.flatMapMany(destinationService::testMethod)
			.subscribe();
	}
}
