package com.k_int.pushKb.interactions.gokb.source;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.k_int.pushKb.interactions.gokb.GokbApiClient;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.services.SourceFeedService;
import com.k_int.pushKb.services.SourceRecordService;

import io.micronaut.core.annotation.NonNull;
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
	private final GokbApiClient gokbApiClient;
	private final SourceRecordService sourceRecordService;

	public GokbFeedService(
    GokbApiClient gokbApiClient,
		SourceRecordService sourceRecordService
  ) {
		this.gokbApiClient = gokbApiClient;
		this.sourceRecordService = sourceRecordService;
	}

	// The actual "Fetch a stream of sourceRecords" method
	public Flux<SourceRecord> fetchSourceRecords(GokbSource source) {
		log.info("GokbFeedService::fetchSourceRecords called for GokbSource: {}", source);
		GokbSourceType gokbSourceType = source.getGokbSourceType();

		return Mono.from(sourceRecordService.findMaxLastUpdatedAtSourceBySource(source))
			.flatMapMany(maxVal -> {
				return this.fetchSourceRecords(gokbSourceType, source.getId(), Optional.ofNullable(maxVal));
			})
			// Is switchIfEmpty the right thing to do here?
			.switchIfEmpty(Flux.from(this.fetchSourceRecords(gokbSourceType, source.getId(), Optional.ofNullable(null))));
	}


	public Flux<SourceRecord> fetchSourceRecords(GokbSourceType gokbSourceType, UUID sourceId, Optional<Instant> changedSince) {
		Instant startTime = Instant.now();
		log.info("LOGDEBUG RAN FOR SOURCE({}, {}) AT: {}",sourceId, gokbSourceType, startTime);

		return fetchPage(gokbSourceType, changedSince)
//			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing... // Do we log each page?
			.expand(currResponse -> this.getNextPage(gokbSourceType, currResponse, changedSince))

			.limitRate(3, 2)
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.flatMapSequential( Flux::fromIterable )
			
			// Convert this JsonNode into a Source record
			.map(jsonNode -> this.handleSourceRecordJson(jsonNode, sourceId) ) // Map the JsonNode to a source record
			.concatMap( sourceRecordService::saveOrUpdateRecord )    // FlatMap the SourceRecord to a Publisher of a SourceRecord (the save)			
			.buffer( 1000 )
			.doOnNext( chunk -> {
				log.info("Saved {} records", chunk.size());
			})
			.doOnError(throwable -> throwable.printStackTrace())
			// TODO is this ok?
			.flatMap(chunk -> Flux.fromIterable(chunk)) // Return from chunk back to regular flux at the end for return type reasons
			// FIXME do we need this (And does this even work?)
			.doOnComplete(() -> log.info("LOGDEBUG FINISHED FOR SOURCE({}, {}) AT: {}", sourceId, gokbSourceType, Instant.now()));
	}

	protected Mono<GokbScrollResponse> fetchPage(@NonNull GokbSourceType gokbSourceType, @NonNull Optional<String> scrollId, Optional<Instant> changedSince ) {
		if (gokbSourceType == GokbSourceType.TIPP) {
			return Mono.from(gokbApiClient.scrollTipps(scrollId.orElse(null), changedSince.orElse(null)));
		} else if (gokbSourceType == GokbSourceType.PACKAGE) {
			return Mono.from(gokbApiClient.scrollPackages(scrollId.orElse(null), changedSince.orElse(null)));
		}

		// What to do if we hit an unknown GokbSourceType??
		return Mono.empty();
	}
  
	protected Mono<GokbScrollResponse> fetchPage(GokbSourceType gokbSourceType, Optional<Instant> changedSince) {
		return fetchPage(gokbSourceType, Optional.empty(), changedSince);
	}

		protected Mono<GokbScrollResponse> fetchPage(GokbSourceType gokbSourceType) {
		return fetchPage(gokbSourceType, Optional.empty(), Optional.empty());
	}
  
  protected Mono<GokbScrollResponse> getNextPage(final GokbSourceType gokbSourceType, final @NonNull GokbScrollResponse currentResponse, Optional<Instant> changedSince) {
  	log.info("Generating next page subscription");
  	boolean more = currentResponse.isHasMoreRecords() && currentResponse.getSize() > 0;
  	
  	if (!more) {
  		Instant endTime = Instant.now();
			// Not sure about logging this tbh
  		log.info( "Fetched last page: {}", endTime);
  		return Mono.empty();
  	}
  	return fetchPage(gokbSourceType, Optional.ofNullable(currentResponse.getScrollId()), changedSince )
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
