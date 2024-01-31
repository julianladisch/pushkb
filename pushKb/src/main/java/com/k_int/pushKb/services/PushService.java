package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.proteus.ProteusService;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import com.k_int.proteus.ComponentSpec;

@Singleton
@Slf4j
public class PushService {
  private final SourceService sourceService;
	private final SourceRecordService sourceRecordService;
  private final PushTaskService pushTaskService;

	// FIXME This will need incorporating later
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	public PushService(
    SourceService sourceService,
		SourceRecordService sourceRecordService,
    PushTaskService pushTaskService,
		ProteusService proteusService,
    ObjectMapper objectMapper
	) {
    this.sourceService = sourceService;
		this.sourceRecordService = sourceRecordService;
    this.pushTaskService = pushTaskService;
		this.proteusService = proteusService;
		this.objectMapper = objectMapper;
	}

  // TO TEST ALGORITHM
	// Ingest _some_ records
	// Build a version of algo with a 1% failure rate
	// Log out each one as "sent"
	// Save to some destination_record class
	// -- pointers
	//        lastSent
	//        latestSent
	//        unbrokenMax (Names need work)
	// -- running boolean?

	// Allow to run on schedule
	// Logging
	//     Current pointers from destination_record
	//     Head of source_records list
	//     For each record log out id, then either SENT (ID) or ERROR (ID) (10% failure)

  public Mono<PushTask> runPushTask(PushTask pt) {
    // We potentially need to run this twice, once to zip up hole,
    //once to bring in line with latest changes
    return Mono.from(runPushTaskSingle(pt))
      // At this point, we should ALWAYS have "zipped up" the gap in records, next check if any new ones exist ahead of DHP
      //.doOnNext(dest -> log.info("WHEN DO WE SEE THIS? {}", dest))
				// Go lookup head of stream (PT sourceid should never have changed...)
      .zipWith(
        // Grab sourceId from pt, and then use that to grab Instant head of source feed
        // Again we assume that id is unique acrosss sourceTypes
        Mono.from(sourceRecordService.findMaxUpdatedBySourceId(pt.getSourceId()))
      )
      .flatMap(tuple -> {
        PushTask lastPt = tuple.getT1();
        Instant sourceRecordHead = tuple.getT2();
        if (sourceRecordHead.compareTo(lastPt.getDestinationHeadPointer()) > 0) {
          // There are fresh records ahead of the footPointer, rerun from lastPT
          // ATM we do this specifically as a second run, we could recurse this, but honestly running once an hour should be fine
          log.info("Fresh records exist ahead of destinationHeadPointer, bringing in line");
          return runPushTaskSingle(lastPt);
        }

        // No need to rerun, just 
        return Mono.just(pt);
      });
  }

  public Mono<PushTask> runPushTaskSingle(PushTask pt) {
    log.info("runPushTaskSingle called with {}", pt);
    Instant lowerBound = pt.getFootPointer();
    Instant upperBound = Instant.now();

    if (pt.getDestinationHeadPointer().compareTo(pt.getFootPointer()) != 0) {
      // If the DHP is NOT equal to FP, then we are in the "gap", continue from LSP
      upperBound = pt.getLastSentPointer();
    } // Otherwise, use head of stack with Instant.now()

    log.info("UPPER BOUND: {}", upperBound);
    log.info("LOWER BOUND: {}", lowerBound);


    // FIXME this needs to come from the PT transform model somehow
    ComponentSpec<Object> proteusSpec = proteusService.loadSpec("GOKBScroll_TIPP_ERM6_transform.json");

    return Flux.from(sourceRecordService.getSourceRecordFeedBySourceId(
			pt.getSourceId(),
			// TODO what happens if we have two records with the same timestamp? - Unlikely but possible I guess
			//Instant.EPOCH,
			lowerBound,
			//Instant.now()
			upperBound
		))
    .buffer(100)
		.flatMap(srArray -> {
      //log.info("SR ARRAY: {}", srArray);

      // We need to build the JsonNode "records" field and then apply to output
      ArrayList<JsonNode> recordsList = new ArrayList<JsonNode>();

      // TODO can we parallelise the transformation of these 100 records?
      for(SourceRecord sr : srArray) {
        try {
          // Am not convinced that converting one by one is the way to go,
          // perhaps a standard proteusSpec for converting an array of records
          // which can reference a spec we feed in? 
          recordsList.add(proteusService.convert(
            proteusSpec,
            sr.getJsonRecord()
          ));
        } catch (Exception e) {
          e.printStackTrace();
          // FIXME
          // Is this the right way to error?
          return Mono.error(e);
        }
      }

      // Set up output
      // FIXME this isn't how we'll manage sessions and chunks
      JsonNode pushKBJsonOutput = JsonNode.createObjectNode(Map.ofEntries(
        new AbstractMap.SimpleEntry<String, JsonNode>("sessionId", JsonNode.createStringNode("test-session-1")),
        new AbstractMap.SimpleEntry<String, JsonNode>("chunkId", JsonNode.createStringNode("chunk1")),
        new AbstractMap.SimpleEntry<String, JsonNode>("records", JsonNode.createArrayNode(recordsList))
      ));

      log.info("pushKBJsonOutput: {}", pushKBJsonOutput);
      /* TODO
       * Convert list of SRs into single JSON output
       */
      /*
       * Idk if this is useful, but this is how to set up our own tuple. Could be useful for passing info downstream
       * Tuple2<SourceRecord, PushTask> output = Tuples.of(sr, pt);
       */

      // We check the very bottom sourceRecord, as that's where the pointer will move
      SourceRecord firstSr = srArray.get(0);
      SourceRecord sr = srArray.get(srArray.size() - 1);
      
      // FIXME these logs are not helpful wording yet

  
      // For now, assume repository has properly fetched everything it needs to
      // TODO check what happens if this run is empty
      // At this point we need to send this record

      // TODO Replace with actual send logic
      try {
        log.info("SENDING JSON OUTPUT: {}", objectMapper.writeValueAsString(pushKBJsonOutput));
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Fixme forget this random bit
/*       Random random = new Random();
      // 10% failure rate
      if (random.nextDouble() <= 0.1) {
        return Mono.error(new Exception("SOMETHING WENT WRONG HERE"));
      }
 */
      log.info("SENT RECORD: {}", sr.getId());
      // ASSUMING right now it's successful, so sr.getUpdated() is new relevant data
      
      // The last thing "successfully sent" was the last timestamp
      pt.setLastSentPointer(sr.getUpdated());

      // Now compare the earliest thing sent in this chunk to the DHP
      if (firstSr.getUpdated().compareTo(pt.getDestinationHeadPointer()) > 0) {
        // We have a new head pointer
        pt.setDestinationHeadPointer(firstSr.getUpdated());
      }

			return pushTaskService.update(pt);
		})
    /*
     * TODO does last trigger if there's an error?
     */
    .takeLast(1) // This will contain the PT as it stands after the LAST record in the stack
    .next() // This should be a passive version of last(), where if stream is empty we don't get a crash
    .flatMap(lastPt -> {
      log.info("We still got here after exception though");
      /* If previous footPointer is updated
       * then the between logic will return a list NOT containing
       * a record equal to or below FootPointer.
       * 
       * If we have reached the end of the list successfully then the destinationHeadPointer
       * should be the new footPointer. ASSUMES THAT THE BETWEEN WORKS AS EXPECTED
       */
      // Is this if necessary? DHP should always be ahead of FP
      if (lastPt.getDestinationHeadPointer().compareTo(pt.getFootPointer()) > 0 ) {
        // We reached the end but never moved footPointer
        lastPt.setFootPointer(lastPt.getDestinationHeadPointer());
        return Mono.from(pushTaskService.update(lastPt)); 
      }

      // If nothing changed, just return PT as is
      return Mono.just(lastPt);
      /* 
       * TODO
       * DOWNSTREAM will need to inspect the changed PT and make decisions
       * about continuing/moving on based on pointers and head of record stack
       */
    });
  }
}
