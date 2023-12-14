package com.k_int.pushKb.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Publisher;

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

	// FIXME remove this
	private final SourceRecordRepository sourceRecordRepository;

	// FIXME remove this too
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	public SchedulingService(
		GoKBFeedService goKBFeedService,
		SourceRecordRepository sourceRecordRepository,
		SourceService sourceService,
		ProteusService proteusService,
		ObjectMapper objectMapper
	) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordRepository = sourceRecordRepository;
		this.sourceService = sourceService;
		this.proteusService = proteusService;
		this.objectMapper = objectMapper;
	}


	// TESTING
	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testProteus() {
		log.info("TESTING PROTEUS");
			Mono.from(sourceRecordRepository.findTop2OrderByCreatedDesc())
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
	}

  // FIXME need to work on delay here
  /* @Scheduled(initialDelay = "1s", fixedDelay = "1h")
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
	} */

/* 	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testingStreams() {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int a = 10000; a > 0; a--) {
			array.add(a);
		}

		Flux.fromIterable(array)
				.buffer(100)
				.limitRate(3)
				.doOnNext(chunk -> log.info("WHAT IS CHUNK? {}", chunk))
				.doOnNext(chunk -> {
					if (chunk.contains(356)) {
						throw new RuntimeException("WHOOPS");
					}
				})
				.subscribe();
	} */
}
