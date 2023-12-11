package com.k_int.pushKb.services;

import java.time.Instant;
import java.time.Duration;

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
import jakarta.validation.constraints.Email;
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
		Instant startTime = Instant.now();
		log.info("LOGDEBUG RAN AT: {}", startTime);

		Mono.from(gokbApiClient.scrollTipps(null, null))
//			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing... // Do we log each page?
			.expand(scrollResponse -> {
//				log.info("SR HASMORERECORDS: {}", scrollResponse.isHasMoreRecords());
				
				boolean more = scrollResponse.isHasMoreRecords() && scrollResponse.getSize() > 0;
				
				if (!more) {
					Instant endTime = Instant.now();
					log.info( "Finished ingesting at: {}", endTime);
					log.info( "Time taken {}", Duration.between(startTime, endTime) );
					return Mono.empty();
				}
				
				return Mono.from(gokbApiClient.scrollTipps(scrollResponse.getScrollId(), null))
					.doOnSubscribe(_s -> log.info("Fetching next GOKB page") );
			})

			.limitRate(3, 2)
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.concatMap( Flux::fromIterable )
			
			// Convert this JsonNode into a Source record
			.flatMap( this::handleSourceRecordJson ) // Map the JsonNode to a source record
			.flatMap( sourceRecordService::saveRecord )    // FlatMap the SourceRecord to a Publisher of a SourceRecord (the save)			
			
			.buffer( 500 )
			
			.doOnNext( chunk -> {
				log.info("Saved {} records", chunk.size());
			})
			.doOnError(throwable -> throwable.printStackTrace())
			.subscribe();
	}

	private Publisher<SourceRecord> handleSourceRecordJson ( @NonNull JsonNode jsonNode ) {
		// TODO this source is hardcoded rn, but should be found from boostrapped data
		return Mono.from(sourceService.findBySourceUrlAndCodeAndSourceType(
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
//		log.info( "Saved record {}", sr);
	}
}
