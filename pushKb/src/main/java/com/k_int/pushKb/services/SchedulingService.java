package com.k_int.pushKb.services;

import static java.util.Collections.checkedSortedMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.Boostraps;
import com.k_int.pushKb.interactions.folio.destination.FolioDestination;
import com.k_int.pushKb.interactions.folio.destination.FolioDestinationApiService;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;

// FIXME think this will be removed once I'm done debugging Proteus usage -- future task though
import com.k_int.pushKb.proteus.ProteusService;

import io.micronaut.http.client.HttpClient;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
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

	// FIXME remove this too
	// SRS not needed long term, here for example usage with Proteus
	private final SourceRecordService sourceRecordService;
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	// Not sure about this
	private final HttpClient httpClient;

	public SchedulingService(
		SourceRecordService sourceRecordService,
		SourceService sourceService,
		PushTaskService pushTaskService,
		PushService pushService,
		DestinationService destinationService,
		ProteusService proteusService,
		ObjectMapper objectMapper,
		HttpClient httpClient //TODO Keep an eye on this
	) {
		this.sourceRecordService = sourceRecordService;
		this.sourceService = sourceService;
		this.pushTaskService = pushTaskService;
		this.pushService = pushService;
		this.destinationService = destinationService;
		this.proteusService = proteusService;
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
	}

	// TESTING
/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testProteus() {
		log.info("TESTING PROTEUS");
			Flux.from(sourceRecordService.findTop2OrderByCreatedDesc())
				.map(sr -> {
					try {
						JsonNode jsonOutput = proteusService.convert(
							proteusService.loadSpec("GOKBScroll_TIPP_ERM6_transform.json"),
							sr.getJsonRecord()
						);

						return jsonOutput;
					} catch (Exception e) {
						e.printStackTrace();
						return JsonNode.nullNode();
					}
				})
				.doOnNext(jsonOutput -> {
					try {
						log.info("JSON OUTPUT: {}", objectMapper.writeValueAsString(jsonOutput));
					} catch (Exception e) {
						e.printStackTrace();
					}
				})
				.subscribe();
	} */

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
			.subscribe(); */

		// Example grabbing bootstrapped TIPP Source directly, not necessary now
/* 		Mono.from(sourceService.findById(
			GokbSource.generateUUIDFromSource((GokbSource) Boostraps.sources.get("GOKB_TIPP")),
			GokbSource.class
		))
			.flatMapMany(sourceService::triggerIngestForSource)
			.subscribe(); 
	}*/

	// FETCHING FROM FOLIO---?
	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		// For now, grab our FolioDestination from Bootstraps directly
		Mono.from(destinationService.findById(
			FolioDestination.class,
			FolioDestination.generateUUIDFromDestination(
				(FolioDestination) Boostraps.destinations.get("LOCAL_RANCHER_FOLIO")
			)
			/* FolioDestination.generateUUIDFromDestination(
				(FolioDestination) Boostraps.destinations.get("SNAPSHOT2")
			) */
		))
			.flatMapMany(destinationService::testMethod)
			.subscribe();
	}
}
