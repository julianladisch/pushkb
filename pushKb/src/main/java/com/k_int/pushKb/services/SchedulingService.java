package com.k_int.pushKb.services;


import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k_int.proteus.ComponentSpec;
import com.k_int.proteus.Context;
import com.k_int.proteus.Input;

@Singleton
@Slf4j
public class SchedulingService {
	private final GoKBFeedService goKBFeedService;
	// FIXME This probably shouldn't be here
	private final SourceService sourceService;

	// FIXME remove this
	private final SourceRecordRepository sourceRecordRepository;

	public SchedulingService(
		GoKBFeedService goKBFeedService,
		SourceRecordRepository sourceRecordRepository,
		SourceService sourceService
	) {
		this.goKBFeedService = goKBFeedService;
		this.sourceRecordRepository = sourceRecordRepository;
		this.sourceService = sourceService;
	}

	// FIXME This is temporary
	static Object loadJson(String fileName) throws IOException {
    return new ObjectMapper()
        .readValue(
            new FileInputStream(fileName),
            Object.class
    );
	}

	// TESTING
	@Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void testProteus() {
		log.info("TESTING PROTEUS");
		try {
			ComponentSpec<Object> spec = ComponentSpec.loadFile("src/main/resources/transformSpecs/GOKBScroll_TIPP_ERM6_transform.json");
			Context context = Context.builder().spec(spec).build();
			Input input = new Input(loadJson("src/main/resources/transformSpecs/input.json"));

			Object result = context
					.inputMapper(input)
					.getComponent()
					.orElse(null);
			log.info("OUTPUT: {}", result);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
