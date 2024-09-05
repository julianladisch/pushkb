package com.k_int.pushKb.interactions.gokb.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import java.net.MalformedURLException;

import com.k_int.pushKb.interactions.gokb.GokbApiClient;
import com.k_int.pushKb.interactions.gokb.model.GokbScrollResponse;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.services.HttpClientService;
import com.k_int.pushKb.services.SourceFeedService;
import com.k_int.pushKb.services.SourceRecordDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.client.HttpClient;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@ExecuteOn(TaskExecutors.BLOCKING)
@Singleton
public class GokbFeedService implements SourceFeedService<GokbSource> {
	private final SourceRecordDatabaseService sourceRecordDatabaseService;
	private final HttpClientService httpClientService;

	public GokbFeedService(
		SourceRecordDatabaseService sourceRecordDatabaseService,
		HttpClientService httpClientService
  ) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
		this.httpClientService = httpClientService;
	}

	// Dynamically set up GokbApiClient from source
	// FIXME needs refactoring
	GokbApiClient getGokbClient(GokbSource source) throws MalformedURLException {
		// GokbApi Client will be gokb url + "/gokb/api". See domain class for details
		HttpClient client = httpClientService.create(source.getSourceUrl());

		return new GokbApiClient(client);
	}
	

	// The actual "Fetch a stream of sourceRecords" method
	public Flux<SourceRecord> fetchSourceRecords(GokbSource source) {
		log.info("GokbFeedService::fetchSourceRecords called for GokbSource: {}", source);
		try {
			GokbApiClient client = getGokbClient(source);
			return Mono.from(sourceRecordDatabaseService.findMaxLastUpdatedAtSourceBySource(source))
				.flatMapMany(maxVal -> {
					return this.fetchSourceRecords(source, client, Optional.ofNullable(maxVal));
				})
				// Is switchIfEmpty the right thing to do here?
				.switchIfEmpty(Flux.from(this.fetchSourceRecords(source, client, Optional.ofNullable(null))));
		} catch (MalformedURLException e) {
			return Flux.error(e);
		}
	}

	public Flux<SourceRecord> saveSourceRecordChunk(List<JsonNode> incomingRecords, GokbSource source) {
		return Flux.fromIterable(incomingRecords)
			.map(jsonNode -> this.handleSourceRecordJson(jsonNode, source.getId()))
			.flatMap( sourceRecordDatabaseService::saveOrUpdateRecord ) // Make sure we save the record (allow saves to happen)
			.doOnComplete(() -> log.info("Saved {} records", incomingRecords.size()));
			//.flatMap(sourceRecordDatabaseService::saveOrUpdateRecord);
	}

	public Flux<SourceRecord> fetchSourceRecords(GokbSource source, GokbApiClient client, Optional<Instant> changedSince) {
		Instant startTime = Instant.now();
		log.info("LOGDEBUG RAN FOR SOURCE({}, {}) AT: {}", source.getId(), source.getGokbSourceType(), startTime);

		return fetchPage(source, client, Optional.empty(), changedSince)
//			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing... // Do we log each page?
			.expand(currResponse -> this.getNextPage(source, client, currResponse, changedSince))

			.limitRate(3, 2) // What if we don't limit this rate?
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.flatMapSequential( Flux::fromIterable )
			.buffer( 1000 )
			.concatMap(jsonRecords -> saveSourceRecordChunk(jsonRecords, source));
			// Convert this JsonNode into a Source record
			//.map(jsonNode -> this.handleSourceRecordJson(jsonNode, source.getId()) ) // Map the JsonNode to a source record
			//.concatMap( sourceRecordDatabaseService::saveOrUpdateRecord )    // FlatMap the SourceRecord to a Publisher of a SourceRecord (the save)			
			//.buffer( 1000 )
			//.doOnNext( chunk -> {
			//	log.info("Saved {} records", chunk.size());
			//})
			//.flatMap(chunk -> Flux.fromIterable(chunk)); // Return from chunk back to regular flux at the end for return type reasons
	}

	protected Mono<GokbScrollResponse> fetchPage(@NonNull GokbSource source, @NonNull GokbApiClient client, @NonNull Optional<String> scrollId, Optional<Instant> changedSince ) {
		if (source.getGokbSourceType() == GokbSourceType.TIPP) {
			return Mono.from(client.scrollTipps(scrollId.orElse(null), changedSince.orElse(null)));
		} else if (source.getGokbSourceType() == GokbSourceType.PACKAGE) {
			return Mono.from(client.scrollPackages(scrollId.orElse(null), changedSince.orElse(null)));
		}

		// What to do if we hit an unknown GokbSourceType??
		return Mono.error(new RuntimeException("Unknown gokbSourceType: " + source.getGokbSourceType()));
	}
  
/* 	protected Mono<GokbScrollResponse> fetchPage(GokbSource source, GokbApiClient client, Optional<Instant> changedSince) {
		return fetchPage(source, Optional.empty(), changedSince);
	}

	protected Mono<GokbScrollResponse> fetchPage(GokbSource source) {
		return fetchPage(source, Optional.empty(), Optional.empty());
	} */
  
  protected Mono<GokbScrollResponse> getNextPage(final GokbSource source, final GokbApiClient client, final @NonNull GokbScrollResponse currentResponse, Optional<Instant> changedSince) {
  	log.info("Generating next page subscription");
  	boolean more = currentResponse.isHasMoreRecords() && currentResponse.getSize() > 0;
  	
  	if (!more) {
  		Instant endTime = Instant.now();
			// Not sure about logging this tbh
  		log.info( "Fetched last page: {}", endTime);
  		return Mono.empty();
  	}
  	return fetchPage(source, client, Optional.ofNullable(currentResponse.getScrollId()), changedSince )
  		.doOnSubscribe(_s -> log.info("Fetching next GOKB page") );
  }

	private SourceRecord handleSourceRecordJson ( @NonNull JsonNode jsonNode, UUID sourceId ) {
		String sourceUUID = jsonNode.get("uuid").getStringValue();
		SourceRecord sr = SourceRecord.builder()
		.jsonRecord(jsonNode)
		.lastUpdatedAtSource(Instant.parse(jsonNode.get("lastUpdatedDisplay").getStringValue()))
		.sourceUUID(sourceUUID)
		.sourceType(GokbSource.class)
		.sourceId(sourceId)
		.build();

		sr.setId(SourceRecord.generateUUIDFromSourceRecord(sr));
		
		return sr;
	}

	protected void handleNode(SourceRecord sr) {
//		log.info( "Saved record {}", sr);
	}
}
