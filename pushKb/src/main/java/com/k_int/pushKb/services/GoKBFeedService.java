package com.k_int.pushKb.services;

import java.time.Instant;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.gokb.GokbApiClient;
import com.k_int.pushKb.gokb.GokbScrollResponse;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.model.SourceType;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class GoKBFeedService {
	private final GokbApiClient gokbApiClient;
	private final SourceRecordService sourceRecordService;
	private final SourceService sourceService;

	public GoKBFeedService(
    GokbApiClient gokbApiClient,
		SourceRecordService sourceRecordService,
		SourceService sourceService
  ) {
		this.gokbApiClient = gokbApiClient;
		this.sourceRecordService = sourceRecordService;
		this.sourceService = sourceService;
	}

  @ExecuteOn(TaskExecutors.BLOCKING)
	public void fetchGoKBTipps() {
		log.info("LOGDEBUG RAN AT: {}", Instant.now());

		Mono.from(gokbApiClient.scrollTipps(null, null))
			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing... // Do we log each page?
			.expand(scrollResponse -> {
				log.info("SR HASMORERECORDS: {}", scrollResponse.isHasMoreRecords());
				if (
					!scrollResponse.isHasMoreRecords() ||
					scrollResponse.getScrollId() == null ||
					scrollResponse.getScrollId().equals("")
				) {
					return Mono.empty();
				} else {
					return Mono.from(gokbApiClient.scrollTipps(scrollResponse.getScrollId(), null)).doOnNext(page -> log.info("LOGDEBUG WHAT IS THING (INTERNAL): {}", page));
				}
			})
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.flatMap( Flux::fromIterable )
			// Convert this JsonNode into a Source record
			.flatMap( this::handleSourceRecordJson ) // Map the JsonNode to a source record
			.flatMap( sourceRecordService::saveRecord )    // FlatMap the SourceRecord to a Publisher of a SourceRecord (the save)
			
			// Reference the method instead of inlining it.
			.subscribe(this::handleNode);
	}

	// FIXME want to bring this out, so we can inspect gokbSR down the line, and use that to trigger scroll again
	private Publisher<Object> handleScrollResponse(GokbScrollResponse gokbSR) {
		return Flux.fromIterable(gokbSR.getRecords());
	}


	private Publisher<SourceRecord> handleSourceRecordJson ( @NonNull JsonNode jsonNode ) {
		// TODO this source is hardcoded rn
		return Mono.from(sourceService.ensureSource(
			"https://gokb.org/gokb/api",
			SourceCode.GOKB,
			SourceType.TIPP
		)).map(source -> this.buildSourceRecord(jsonNode, source));
	}

  private SourceRecord buildSourceRecord ( @NonNull JsonNode jsonNode, Source source ) {
  	return SourceRecord.builder()
			.jsonRecord(jsonNode)
			.source(source)
			.build();
  }
	
	protected void handleNode(SourceRecord sr) {
		log.info( "Saved record {}", sr);
	}
}
