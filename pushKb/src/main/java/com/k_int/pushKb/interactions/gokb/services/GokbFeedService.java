package com.k_int.pushKb.interactions.gokb.services;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	private final GokbSourceDatabaseService gokbSourceDatabaseService;
	private final HttpClientService httpClientService;

	public GokbFeedService(
		SourceRecordDatabaseService sourceRecordDatabaseService,
		GokbSourceDatabaseService gokbSourceDatabaseService,
		HttpClientService httpClientService
  ) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
		this.gokbSourceDatabaseService = gokbSourceDatabaseService;
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
	public Mono<GokbSource> fetchSourceRecords(GokbSource source) {
		log.info("GokbFeedService::fetchSourceRecords called for GokbSource: {}", source);
		try {
			GokbApiClient client = getGokbClient(source);
			return Mono.just(Optional.ofNullable(source.getPointer()))
				.flatMapMany(pointer -> { // Pointer may be Optional(null)
					return this.fetchSourceRecords(source, client, pointer);
				})
				.takeLast(1)
				.next() // takeLast(1).next goes from Flux -> Mono on last item
				.flatMap(lastSource -> Mono.just(lastSource));
		} catch (MalformedURLException e) {
			return Mono.error(e);
		}
	}

	public Mono<GokbSource> saveSourceRecordsFromPage(List<JsonNode> incomingRecords, GokbSource source) {
		return Flux.fromIterable(incomingRecords)
			.map(jsonNode -> this.handleSourceRecordJson(jsonNode, source.getId()))
			// Make sure we save the record. This can happen in any order in theory,
			// store max pointer from this page at end so if it dies midway we can pick up from _last_ page
			// FIXME this assumes the feed is in "lastUpdatedDisplay" order, which does NOT seem to be the case for TIPPs
			.flatMap( sourceRecordDatabaseService::saveOrUpdateRecord )
			.reduce( // Reduce Flux down to Mono<Instant> for latestSeen record
				Optional.ofNullable(source.getPointer()).orElse(Instant.EPOCH), // Only update pointer if we've moved _forward_.
				// FIXME This WILL NOT WORK if feed is not in order of lastUpdatedAtSource, which appears to be the case for TIPP...
				(acc, sourceRecord) -> {
					Instant curr = sourceRecord.getLastUpdatedAtSource();
					
					// STORE LATEST SOURCE RECORD "updated" IN T2
					if (curr.isAfter(acc)) {
						return curr;
					}
					return acc;
				}
			).flatMap(latestUpdatedAtSource -> {
				log.info("Saved {} records", incomingRecords.size()); // This is emitted after reduce finalises
				log.info("LATEST SEEN UPDATED AT SOURCE: {}", latestUpdatedAtSource); // FIXME REMOVE THIS DEV LOGGING
				source.setPointer(latestUpdatedAtSource);

				return Mono.from(gokbSourceDatabaseService.saveOrUpdate(source));
			});
	}


	public Flux<GokbSource> fetchSourceRecords(GokbSource source, GokbApiClient client, Optional<Instant> changedSince) {
		Instant startTime = Instant.now();
		log.info("LOGDEBUG RAN FOR SOURCE({}, {}) AT: {}", source.getId(), source.getGokbSourceType(), startTime);

		return fetchPage(source, client, Optional.empty(), changedSince)
//			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing... // Do we log each page?
			.expand(currResponse -> this.getNextPage(source, client, currResponse, changedSince))

			.limitRate(3, 2) // What if we don't limit this rate? -- I think it's slower...
			.map(GokbScrollResponse::getRecords)
			.concatMap(responsePage -> saveSourceRecordsFromPage(responsePage, source));
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
  
  protected Mono<GokbScrollResponse> getNextPage(
		final GokbSource source,
		final GokbApiClient client,
		final @NonNull GokbScrollResponse currentResponse,
		Optional<Instant> changedSince
	) {
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

		// We can shortcut the "is this a package or a tipp source" work here
		// by simply checking if the "tippPackageUUID" node is null
		JsonNode packageUUIDNode = jsonNode.get("tippPackageUuid");
		String packageUUID = null;
		if (packageUUIDNode != null) {
			packageUUID = packageUUIDNode.getStringValue();
		}

		SourceRecord sr = SourceRecord.builder()
		.filterContext(packageUUID) // This will be null for package source records.
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
