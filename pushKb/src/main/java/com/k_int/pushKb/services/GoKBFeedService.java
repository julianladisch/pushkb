package com.k_int.pushKb.services;

import java.time.Instant;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.k_int.pushKb.gokb.GoKBRecord;
import com.k_int.pushKb.gokb.GokbApiClient;
import com.k_int.pushKb.gokb.GokbScrollResponse;


@Slf4j
@Singleton
public class GoKBFeedService {
  private final GokbApiClient gokbApiClient;
  public GoKBFeedService(GokbApiClient gokbApiClient) { 
    this.gokbApiClient = gokbApiClient;
  }

  public void testScheduling() {
    log.info("LOGDEBUG RAN AT: {}", Instant.now());
    Mono<GokbScrollResponse> mono = Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null));
    mono.subscribe(thing -> {
      log.info("LOGDEBUG WHAT IS THING: {}", thing);

      log.info("Checking records");
      for (GoKBRecord record : thing.getRecords()) {
        log.info("Record: {}", record);
      }
    });
	}
}
