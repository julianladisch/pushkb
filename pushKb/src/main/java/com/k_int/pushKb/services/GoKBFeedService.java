package com.k_int.pushKb.services;

import java.time.Instant;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.gokb.GokbApiClient;
import com.k_int.pushKb.gokb.GokbScrollResponse;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.model.SourceRecordType;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class GoKBFeedService {
	private final GokbApiClient gokbApiClient;
  private final SourceRecordRepository sourceRecordRepository;

	public GoKBFeedService(
    GokbApiClient gokbApiClient,
    SourceRecordRepository sourceRecordRepository
  ) {
		this.gokbApiClient = gokbApiClient;
    this.sourceRecordRepository = sourceRecordRepository;
	}

//	@ExecuteOn(TaskExecutors.BLOCKING)
//	public void testScheduling() {
//		log.info("LOGDEBUG RAN AT: {}", Instant.now());
//		
//		Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null))
//			.doOnNext(page ->  log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing...
//			.flatMapMany( scrollResponse -> Flux.fromIterable(scrollResponse.getRecords()) )
//			.subscribe(jsonNode -> {
//				try {
//					log.info("Record: {}", objectMapper.writeValueAsString(jsonNode));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//		});
//	}
	
	// Equivalent to the above but with method references.
  // TODO does this need to be separate method to fetchGoKBTipps?
  @ExecuteOn(TaskExecutors.BLOCKING)
	public void fetchGoKBPackages() {
		log.info("LOGDEBUG RAN AT: {}", Instant.now());
		
		Mono.from(gokbApiClient.scroll(GokbApiClient.COMPONENT_TYPE_PACKAGE, null, null))
			.doOnNext(page -> log.info("LOGDEBUG WHAT IS THING: {}", page)) // Log the single thing...
			.map( GokbScrollResponse::getRecords ) // Map returns a none reactive type. FlatMap return reactive types Mono/Flux.
			.flatMapMany( Flux::fromIterable )
			
			// Convert this JsonNode into a Source record
			.map( this::buildSourceRecord ) // Map the JsonNode to a source record
				.flatMap( this::saveRecord )    // FlatMap the SourceRecord to a Publisher of a SourceRecord (the save)
			
			// Reference the method instead of inlining it.
			.subscribe(this::handleNode);
	}
  

  // Must be protected at least to allow AOP annotations.
  // Adding this method gives us something to hang the transaction from. We also use the @Valid annotation
  // to validate the source record before we save it.
  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  protected Publisher<SourceRecord> saveRecord ( @NonNull @Valid SourceRecord sr ) {
  	return sourceRecordRepository.save(sr);
  }
  
  private SourceRecord buildSourceRecord ( @NonNull JsonNode jsonNode ) {
  	return SourceRecord.builder()
			.jsonRecord(jsonNode)
			.source(SourceCode.GOKB)
			.recordType(SourceRecordType.PACKAGE)
			.build();
  }
	
	protected void handleNode(SourceRecord record) {
		log.info( "Saved record {}", record);
	}
}
