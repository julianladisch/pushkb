package com.k_int.pushKb.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.Boostraps;

// FIXME IDK What I'm doing here yet
//import com.k_int.pushKb.destinations.folio.FolioLowLevelApiClient;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;

import com.k_int.pushKb.proteus.ProteusService;
import com.k_int.pushKb.sources.gokb.GokbFeedService;

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
	private final PushService pushService;

	// FIXME DSL needs to be changed over to PushTask... good luck future Ethan

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
		PushService pushService,
		ProteusService proteusService,
		ObjectMapper objectMapper,
		HttpClient httpClient //TODO Keep an eye on this
	) {
		this.sourceRecordService = sourceRecordService;
		this.sourceService = sourceService;
		this.pushService = pushService;
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
			// Iterate over all DSLs, maybe want to be smarter about this in future
			// TODO we will need two Fluxes happening one after the other... one to fill any holes,
			// then one from head -> top of sent stack

			Flux.from(destinationSourceLinkService.getDestinationSourceLinkFeed())
				// Run first Flux -- shouldn't return til last ?
				.flatMap(pushService::handleSourceRecordsFromDSL)
				.doOnNext(dsl -> log.info("WHEN DO WE SEE THIS FINAL END? {}", dsl))
				.subscribe();
	} */

  // FIXME need to work on delay here
  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
			// Fetch all source implementers from sourceService
			Flux.from(sourceService.getSourceImplementors())
			// For each class implementing Source, list all actual Sources in DB
			.flatMap(sourceService::list)
			// For each source, trigger an ingest of all records
			.flatMap(sourceService::triggerIngestForSource)
			.subscribe();

		// Example grabbing bootstrapped TIPP Source directly, not necessary now
/* 		Mono.from(sourceService.findById(
			GokbSource.generateUUIDFromSource((GokbSource) Boostraps.sources.get("GOKB_TIPP")),
			GokbSource.class
		))
			.flatMapMany(sourceService::triggerIngestForSource)
			.subscribe(); */
	}

	// FETCHING FROM FOLIO---?
/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		FOLIOLowLevelApiClient folioClient = new FOLIOLowLevelApiClient();
		Mono.from(folioClient.getChunks())
			.doOnNext(thing -> log.info("WHAT IS THING: {}", thing))
			.subscribe();
	} */
}
