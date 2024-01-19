package com.k_int.pushKb.services;

import java.time.Instant;

import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.proteus.ProteusService;

import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Singleton
@Slf4j
public class PushService {
	private final SourceRecordService sourceRecordService;
	private final DestinationSourceLinkService destinationSourceLinkService;

	// FIXME This will need incorporating later
	private final ProteusService proteusService;
	private final ObjectMapper objectMapper;

	public PushService(
		SourceRecordService sourceRecordService,
		DestinationSourceLinkService destinationSourceLinkService,
		ProteusService proteusService,
    ObjectMapper objectMapper
	) {
		this.sourceRecordService = sourceRecordService;
		this.destinationSourceLinkService = destinationSourceLinkService;
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
  public Mono<DestinationSourceLink> handleSourceRecordsFromDSL(DestinationSourceLink dsl) {
    // We potentially need to run this twice, once to zip up hole,
    //once to bring in line with latest changes
    return Mono.from(handleSourceRecordsFromDSLSingleRun(dsl))
      .doOnNext(dest -> log.info("WHEN DO WE SEE THIS? {}", dest))
				// Go lookup head of stream (DSL source should never have changed...)
      .zipWith(Mono.from(sourceRecordService.findMaxUpdatedBySource(dsl.getSource())))
      .flatMap(tuple -> {
        DestinationSourceLink lastDsl = tuple.getT1();
        Instant sourceRecordHead = tuple.getT2();
        if (sourceRecordHead.compareTo(lastDsl.getDestinationHeadPointer()) > 0) {
          // There are fresh records ahead of the footPointer, rerun from lastDSL
          log.info("Fresh records exist ahead of destinationHeadPointer, bringing in line");
          return handleSourceRecordsFromDSLSingleRun(lastDsl);
        }

        // No need to rerun, just 
        return Mono.just(dsl);
      });
  }

  public Mono<DestinationSourceLink> handleSourceRecordsFromDSLSingleRun(DestinationSourceLink dsl) {
    Instant lowerBound = dsl.getFootPointer();
    Instant upperBound = Instant.now();
    if (dsl.getDestinationHeadPointer().compareTo(dsl.getFootPointer()) != 0) {
      upperBound = dsl.getLastSentPointer();
    }
    
    return Flux.from(sourceRecordService.getSourceRecordFeedBySource(
			dsl.getSource(),
			// TODO what happens if we have two records with the same timestamp?
			// should our pointer include Id (or just be the sourceRecord itself)?
			//Instant.EPOCH,
			lowerBound,
			//Instant.now()
			upperBound
		))
		.flatMap(sr -> {
      /*
       * Idk if this is useful, but this is how to set up our own tuple. Could be useful for passing info downstream
       * Tuple2<SourceRecord, DestinationSourceLink> output = Tuples.of(sr, dsl);
       */

      // This is the "send" analogue
      // FIXME these logs are not helpful wording yet
      
      // If sr is below to footPointer of DSL then we don't send it and something went wrong
      if (sr.getUpdated().compareTo(dsl.getFootPointer()) < 0) {
        log.info("SHOULDN'T SEE THIS... This record was updated before the foot pointer of this DSL, ignoring");

        // Return DSL as is
        return Flux.just(dsl);
      }
      
      if (sr.getUpdated().compareTo(dsl.getFootPointer()) == 0) {
        log.info("This record represents the footPointer of this DSL, bringing footPointer forward");
        // If footPointer matches lastSent, then destinationHeadPointer should be new footPointer
        

        dsl.setLastSentPointer(sr.getUpdated());
        // This may not be necessary thanks to the last() implementation -- fine for now
        dsl.setFootPointer(dsl.getDestinationHeadPointer());
        return destinationSourceLinkService.update(dsl);
      }

      // If sr is equal to destinationHeadPointer of DSL then we don't send it
      if (sr.getUpdated().compareTo(dsl.getDestinationHeadPointer()) == 0) {
        log.info("This record matches the destinationHeadPointer of this DSL, ignoring");
        // Return DSL as is
        return Flux.just(dsl);
      }
      // At this point we need to send this record

      // TODO actual send logic
      log.info("SENT RECORD: {}", sr.getId());
      // ASSUMING right now it's successful, so sr.getUpdated() is new relevant data
      
      // The last thing "successfully sent" was this timestamp
      dsl.setLastSentPointer(sr.getUpdated());
      if (sr.getUpdated().compareTo(dsl.getDestinationHeadPointer()) > 0) {
        // We have a new head pointer
        dsl.setDestinationHeadPointer(sr.getUpdated());
      }

			return destinationSourceLinkService.update(dsl);
		})
    /*
     * TODO alternative -- can we break out of a Flux and not use Between? Would simplify logic
     * instead working back from DestinationHeadPointer or head of record stack?
     * TODO does last trigger if there's an error?
     */
    .last() // This will contain the DSL as it stands after the LAST record in the stack
    .flatMap(lastDsl -> {
      // FIXME is it ok to update from this new "version" of DSL instead of the passed one?
      /* If previous footPointer is updated
       * then the between logic will return a list NOT containing
       * a record equal to or below FootPointer.
       * 
       * If we have reached the end of the list successfully then the destinationHeadPointer
       * should be the new footPointer. ASSUMES THAT THE BETWEEN WORKS AS EXPECTED
       */

      if (lastDsl.getDestinationHeadPointer().compareTo(dsl.getFootPointer()) > 0 ) {
        // We reached the end but never moved footPointer
        lastDsl.setFootPointer(lastDsl.getDestinationHeadPointer());
        return Mono.from(destinationSourceLinkService.update(lastDsl)); 
      }

      // If nothing changed, just return DSL as is
      return Mono.just(lastDsl);
      /* 
       * TODO
       * DOWNSTREAM will need to inspect the changed DSL and make decisions
       * about continuing/moving on based on pointers and head of record stack
       */
    });
  }
}
