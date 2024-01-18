package com.k_int.pushKb.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.Boostraps.Sources;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
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

	// FIXME remove this too
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	public SchedulingService(
		GoKBFeedService goKBFeedService,
		SourceRecordService sourceRecordService,
		SourceService sourceService,
		ProteusService proteusService,
		ObjectMapper objectMapper
	) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordService = sourceRecordService;
		this.sourceService = sourceService;
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

	// TO TEST ALGORITHM
	// Ingest _some_ records
	// Build a version of algo with a 1% failure rate
	// Log out each one as "sent"
	// Save to some destination_record class
	// -- pointers
	//        lastSent
	//        latestSent
	//        unbrokenMax (Names need work)
	// -- running boolean?

	// Allow to run on schedule
	// Logging
	//     Current pointers from destination_record
	//     Head of source_records list
	//     For each record log out id, then either SENT (ID) or ERROR (ID) (10% failure)

/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testSendAlgorithm() {
		log.info("TESTING PUSH ALGORITHM");
			Flux.from(sourceService.findById(Source.generateUUIDFromSource(Sources.GOKB_TIPP)))
				.flatMap(src -> sourceRecordService.getSourceRecordFeedBySource(
					src,
					// TODO what happens if we have two records with the same timestamp?
					// should our pointer include Id (or just be the sourceRecord itself)?
					Instant.EPOCH,
					//Instant.parse("2024-01-18T15:02:01.880991Z"),
					Instant.now()
					//Instant.parse("2024-01-18T15:02:01.898723Z")
				)
			).doOnNext(sr -> {
				log.info("UPDATED: {}", sr.updated);
			})
			.subscribe();
	} */

  // FIXME need to work on delay here
/*   @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void scheduledTask() {
		Mono.from(sourceService.findById(Source.generateUUIDFromSource(Sources.GOKB_TIPP)))
				.flatMap(this::handleSource)
				.subscribe();
	} */

	public Mono<Instant> handleSource(Source source) {
		return Mono.from(sourceRecordService.findMaxLastUpdatedAtSourceBySource(source))
			// Is it the right thing to do here to use doOnSuccess?
			.doOnSuccess(maxVal -> {
				log.info("MAXIMUM TIMESTAMP FOUND: {}", maxVal);
				goKBFeedService.fetchGoKBTipps(source, Optional.ofNullable(maxVal));
			});
	}
}
