package com.k_int.pushKb.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.Boostraps.Sources;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.proteus.ProteusService;
import com.k_int.pushKb.storage.SourceRecordRepository;

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
	private final GoKBFeedService goKBFeedService;
	// FIXME This probably shouldn't be here
	private final SourceService sourceService;
	private final SourceRecordService sourceRecordService;
	private final DestinationSourceLinkService destinationSourceLinkService;
	private final PushService pushService;

	// FIXME remove this too
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	public SchedulingService(
		GoKBFeedService goKBFeedService,
		SourceRecordService sourceRecordService,
		SourceService sourceService,
		DestinationSourceLinkService destinationSourceLinkService,
		PushService pushService,
		ProteusService proteusService,
		ObjectMapper objectMapper
	) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordService = sourceRecordService;
		this.sourceService = sourceService;
		this.destinationSourceLinkService = destinationSourceLinkService;
		this.pushService = pushService;
		this.proteusService = proteusService;
		this.objectMapper = objectMapper;
	}


	// TESTING
	/* @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testProteus() {
		log.info("TESTING PROTEUS");
			Flux.from(sourceRecordRepository.findTop2OrderByCreatedDesc())
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
		Mono.from(sourceService.findById(Source.generateUUIDFromSource(Sources.GOKB_TIPP)))
				.flatMap(this::handleSource)
				.subscribe();
	}

	// This should be in its own thing
	public Mono<Instant> handleSource(Source source) {
		return Mono.from(sourceRecordService.findMaxLastUpdatedAtSourceBySource(source))
			// Is it the right thing to do here to use doOnSuccess?
			.doOnSuccess(maxVal -> {
				log.info("MAXIMUM TIMESTAMP FOUND: {}", maxVal);
				goKBFeedService.fetchGoKBTipps(source, Optional.ofNullable(maxVal));
			});
	}
}
