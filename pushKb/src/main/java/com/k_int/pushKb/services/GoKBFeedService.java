package com.k_int.pushKb.services;

import java.io.IOException;
import java.time.Instant;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.k_int.pushKb.gokb.GoKBRecord;
import com.k_int.pushKb.gokb.GokbApiClient;
import com.k_int.pushKb.gokb.GokbScrollResponse;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;

@Slf4j
@Singleton
public class GoKBFeedService {
	private final GokbApiClient gokbApiClient;
	private final ObjectMapper objectMapper;

	public GoKBFeedService(GokbApiClient gokbApiClient, ObjectMapper objectMapper) {
		this.gokbApiClient = gokbApiClient;
		this.objectMapper = objectMapper;
	}

	public void testScheduling() {
		log.info("LOGDEBUG RAN AT: {}", Instant.now());
		Mono<GokbScrollResponse> mono = Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null));
		mono.subscribe(thing -> {
			log.info("LOGDEBUG WHAT IS THING: {}", thing);

			log.info("Checking records");
			for (JsonNode record : thing.getRecords()) {
				try {
					log.info("Record: {}", objectMapper.writeValueAsString(record));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
