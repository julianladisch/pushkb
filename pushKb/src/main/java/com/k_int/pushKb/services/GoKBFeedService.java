package com.k_int.pushKb.services;


import java.net.URI;
import java.time.Instant;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
// HTTP STUFFS
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.GoKBScrollAPIPage;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Slf4j
@Singleton
public class GoKBFeedService {
  // FIXME we need a GoKB Client class
  private final HttpClient httpClient;
  private final URI uri;
  public GoKBFeedService(@Client(id = "gokb") HttpClient httpClient) {  
        this.httpClient = httpClient;
        this.uri = UriBuilder.of("https://gokb.org")
          .path("gokb")
          .path("api")
          .path("scroll")
          .queryParam("component_type", "Package")
          //.queryParam("changedSince", null) // TODO this won't be null mostly
          //.queryParam("status", "Current") // I forget if we need this
          .build();
    }

    // FIXME cannot cast some to object and rest to string :()
  public void testScheduling() {
    log.info("LOGDEBUG RAN AT: {}", Instant.now());
    Flux<GoKBScrollAPIPage> flux = Flux.from(fetchReleases());
    flux.subscribe(thing -> {
      log.info("LOGDEBUG LOGGING OUT TEST: {}", thing);
    });
	}

  public Publisher<GoKBScrollAPIPage> fetchReleases() {
    log.info("WHAT IS URI?: {}", uri);
    HttpRequest<?> req = HttpRequest.GET(uri) 
            .header(USER_AGENT, "Micronaut HTTP Client")
            .header(ACCEPT, "application/json"); 
    return httpClient.retrieve(req, Argument.of(GoKBScrollAPIPage.class)); 
  }

  // FIXME doing all as String feels icky
  /* public void testScheduling() {
    log.info("LOGDEBUG RAN AT: {}", Instant.now());
    Flux<String> flux = Flux.from(fetchReleases());
    flux.subscribe(thing -> {
      log.info("LOGDEBUG LOGGING OUT TEST: {}", thing);
    });
	}

  public Publisher<String> fetchReleases() {
    log.info("WHAT IS URI?: {}", uri);
    HttpRequest<?> req = HttpRequest.GET(uri) 
            .header(USER_AGENT, "Micronaut HTTP Client")
            .header(ACCEPT, "application/json"); 
    return httpClient.retrieve(req); 
  } */
}
