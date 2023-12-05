package com.k_int.pushKb.services;

import java.io.IOException;
import java.time.Instant;

import com.k_int.pushKb.gokb.GokbApiClient;
import com.k_int.pushKb.gokb.GokbScrollResponse;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class GoKBFeedService {
	private final GokbApiClient gokbApiClient;
	private final ObjectMapper objectMapper;

	public GoKBFeedService(GokbApiClient gokbApiClient, ObjectMapper objectMapper) {
		this.gokbApiClient = gokbApiClient;
		this.objectMapper = objectMapper;
	}

	@ExecuteOn(TaskExecutors.BLOCKING)
	public void testScheduling() {
		log.info("LOGDEBUG RAN AT: {}", Instant.now());
		
		Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null))
			.doOnNext(page ->  log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing...
			.flatMapMany( scrollResponse -> Flux.fromIterable(scrollResponse.getRecords()) )
			
			// Use flatmap many to convert the single item into many. This method will convert this
		  // stream from a Mono (single element) to a flux (many elements)
			// So I can subscribe and do things with each emitted element
			.subscribe(jsonNode -> {
				try {
					log.info("Record: {}", objectMapper.writeValueAsString(jsonNode));
				} catch (IOException e) {
					e.printStackTrace();
				}
		});
	}
	
	// Equivalent to the above but with method references.
	public void testScheduling2() {
		log.info("LOGDEBUG RAN AT: {}", Instant.now());
		
		Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null))
			.doOnNext(page ->  log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing...
			
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.flatMapMany( Flux::fromIterable ) 
			// Reference the method instead of inlining it.
			.subscribe(this::handleNode);
	}
	
	private void handleNode(JsonNode jsonNode) {
		try {
			log.info("Record: {}", objectMapper.writeValueAsString(jsonNode));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
