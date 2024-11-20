package com.k_int.pushKb.services;

import java.io.IOException;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.k_int.proteus.ComponentSpec;
import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushChunk;
import com.k_int.pushKb.model.PushSession;
import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.proteus.ProteusService;

import io.micronaut.json.tree.JsonNode;
// import io.micronaut.serde.ObjectMapper;
import io.micronaut.transaction.TransactionDefinition.Propagation;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Singleton
@Slf4j
public class PushService {
	private final SourceRecordDatabaseService sourceRecordDatabaseService;
  private final PushableService pushableService;

  private final DestinationService destinationService;
  private final PushSessionDatabaseService pushSessionDatabaseService;
  private final PushChunkDatabaseService pushChunkDatabaseService;

  /* Keep bundleSize consistent between chunking for send and chunking for transform
   * IMPORTANT these cannot be allowed to drift because we're assuming a sequential order
   * that these are being sent in. The chunks can be internally ordered however, but each chunk
   * MUST be in the right order when sent so that we can maintain pointer positions.
   */
  private static final int BUNDLE_SIZE = 1000;


  // FIXME investigate how much this is actually needed
	private final ProteusService proteusService;
	// private final ObjectMapper objectMapper;

	public PushService(
		SourceRecordDatabaseService sourceRecordDatabaseService,
    PushableService pushableService,
		ProteusService proteusService,
    DestinationService destinationService,
    PushSessionDatabaseService pushSessionDatabaseService,
    PushChunkDatabaseService pushChunkDatabaseService
    // ObjectMapper objectMapper
	) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
    this.pushableService = pushableService;
    this.pushSessionDatabaseService = pushSessionDatabaseService;
    this.pushChunkDatabaseService = pushChunkDatabaseService;
		this.proteusService = proteusService;
    this.destinationService = destinationService;
		// this.objectMapper = objectMapper;
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


  // Separate out chunk creation?
  // FIXME Might need refactor this is messy
  protected Mono<Pushable> processChunkForPushable(
    Pushable psh,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session,
    List<SourceRecord> sourceRecordChunk,
    ComponentSpec<JsonNode> proteusSpec
  ) {
    return Mono.from(
      pushChunkDatabaseService.save(
        PushChunk.builder()
          .session(session)
          .build()
      )
    ).flatMap(pushChunk -> processChunkForPushableWithPushChunk(psh, destination, client, session, sourceRecordChunk, pushChunk, proteusSpec));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected Mono<Pushable> processChunkForPushableWithPushChunk(
    Pushable psh,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session,
    List<SourceRecord> sourceRecordChunk,
    PushChunk chunk,
    ComponentSpec<JsonNode> proteusSpec
  ) {
    // First transform the whole sourceRecord
    return Flux.fromIterable(sourceRecordChunk)
        .flatMap(sourceRecord -> {
          try {
            JsonNode transformedRecord = proteusService.convert(
              proteusSpec,
              sourceRecord.getJsonRecord()
            );
            return Mono.just(Tuples.of(sourceRecord, transformedRecord));
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Mono.error(e);
          }
        })
        .collectList()
        // Change List<Tuple2<SourceRecord, JsonNode>> into Tuple3<List<JsonNode>, Instant, Instant>
        // where instants are earliest and latest timestamp in chunk
        .flatMap(list -> {
          return Flux.fromIterable(list)
            .reduce(
              Tuples.of(new ArrayList<JsonNode>(), Instant.now(), Instant.EPOCH),
              (acc, tuple) -> {
                Instant sourceRecordUpdated = tuple.getT1().getUpdated();
                Instant earliestSeen = acc.getT2();
                Instant latestSeen = acc.getT3();
                
                // STORE EARLIEST SOURCE RECORD "updated" IN T3
                if (sourceRecordUpdated.isBefore(earliestSeen)) {
                  earliestSeen = sourceRecordUpdated;
                }
                // STORE LATEST SOURCE RECORD "updated" IN T4
                if (sourceRecordUpdated.isAfter(latestSeen)) {
                  latestSeen = sourceRecordUpdated;
                }
                
                acc.getT1().add(tuple.getT2());
                
                // I think this works... Tuples are immutable so return list
                return Tuples.of(acc.getT1(), earliestSeen, latestSeen);
              }
            );
        })
        .flatMap(TupleUtils.function((recordsList, earliestSeen, latestSeen) -> {  // ConcatMap to ensure that we only start sending chunk B after chunk A has returned
          // Set up chunk here and save?
            JsonNode pushKBJsonOutput = JsonNode.createObjectNode(Map.ofEntries(
              new AbstractMap.SimpleEntry<String, JsonNode>("sessionId", JsonNode.createStringNode(session.getId().toString())),
              new AbstractMap.SimpleEntry<String, JsonNode>("chunkId", JsonNode.createStringNode(chunk.getId().toString())),
              new AbstractMap.SimpleEntry<String, JsonNode>("records", JsonNode.createArrayNode(recordsList))
            ));
          
          // Logging out what gets sent here, quite noisy
          /* try {
            // Just logging out the output here
            log.info("SENDING JSON OUTPUT: {}", objectMapper.writeValueAsString(pushKBJsonOutput));
          } catch (Exception e) {
            e.printStackTrace();
          } */

          return Mono.just(Tuples.of(pushKBJsonOutput, earliestSeen, latestSeen));
        }))
        .flatMap(TupleUtils.function((json, earliestSeen, latestSeen) -> {
    
          log.info("Pushing records {} -> {}", earliestSeen, latestSeen);
    
          return Mono.from(destinationService.push(destination, client, json))
            .flatMap(bool -> {
              // Should just be "true", idk why this would ever be false tbh
    
              // The earliest record "successfully sent" was the "last sent" timestamp, as we're moving _down_ the list
              psh.setLastSentPointer(earliestSeen);
    
              // Now compare the latest updated record sent in this chunk to the DHP
              if (latestSeen.compareTo(psh.getDestinationHeadPointer()) > 0) {
                // We have a new head pointer
                psh.setDestinationHeadPointer(latestSeen);
              }
              return Mono.from(pushableService.update(psh));
            });
    
          //log.info("SENT RECORD: {}", sr.getId());
        }));
  }

  // This is useable from the point of view of TemporaryPushTasks AND PushTasks
  public Mono<Tuple3<Destination, DestinationClient<Destination>, PushSession>> getPushableTuple(Pushable p) {
    return Mono.from(destinationService.findById(
      p.getDestinationType(),
      p.getDestinationId()
    ))
    .flatMap(destination -> {
      return Mono.from(destinationService.getClient(destination))
        .flatMap(client -> {
          return Mono.from(pushSessionDatabaseService.save(
            PushSession.builder()
              .pushableId(p.getId())
              .pushableType(p.getClass())
              .build()
          ))
          .flatMap(pushSession -> {
            return Mono.just(Tuples.of(destination, client, pushSession));
          });
        });
    });
  } // Nested flatMaps isn't very pretty here, but it brings them altogether without multiple Tuple setups and reads

  public Mono<Pushable> runPushable(Pushable p) {
    return getPushableTuple(p)
      .flatMap(TupleUtils.function((destination, client, session) -> runPushable(p, destination, client, session)));
  }

  private Mono<Pushable> runPushable(
    Pushable psh,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session
  ) {
    //log.info("runPushable called for {}", psh);
    // We potentially need to run this twice, once to zip up hole,
    //once to bring in line with latest changes
    return Mono.from(runPushableSingle(psh, destination, client, session))
      // At this point, we should ALWAYS have "zipped up" the gap in records, next check if any new ones exist ahead of DHP
				// Go lookup head of stream (Pushable sourceId should never have changed...)
      .zipWith(
        // Grab sourceId from pushable, and then use that to grab Instant head of source feed
        // Again we assume that id is unique across sourceTypes
        Mono.from(sourceRecordDatabaseService.findTopOfFeed(
          psh.getSourceId(),
          Optional.ofNullable(psh.getFilterContext())
        ))
      )
      .flatMap(tuple -> {
        Pushable lastPsh = tuple.getT1();
        Instant sourceRecordHead = tuple.getT2();
        if (sourceRecordHead.compareTo(lastPsh.getDestinationHeadPointer()) > 0) {
          // There are fresh records ahead of the footPointer, rerun from lastPT
          // ATM we do this specifically as a second run, we could recurse this, but honestly running once an hour should be fine
          log.info("Fresh records exist ahead of destinationHeadPointer, bringing in line");
          return runPushableSingle(lastPsh, destination, client, session);
        }

        // No need to rerun, just
        return Mono.just(psh);
      });
  }

  public Mono<Pushable> runPushableSingle(
    Pushable psh,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session
  ) {
    //log.info("runPushableSingle called with {}", psh);

    final Instant lowerBound = psh.getFootPointer();
    Instant upperBound = Instant.now();

    if (psh.getDestinationHeadPointer().compareTo(psh.getFootPointer()) != 0) {
      // If the DHP is NOT equal to FP, then we are in the "gap", continue from LSP
      upperBound = psh.getLastSentPointer();
    } // Otherwise, use head of stack with Instant.now()
    final Instant finalUpperBound = upperBound;

    log.info("UPPER BOUND: {}", upperBound);
    log.info("LOWER BOUND: {}", lowerBound);

    // FIXME this needs to come from the Pushable transform model somehow
    ComponentSpec<JsonNode> proteusSpec = proteusService.loadSpec("GOKBScroll_TIPP_ERM6_transform.json");

    return Mono.from(sourceRecordDatabaseService.countFeed(
      psh.getSourceId(),
      lowerBound,
      finalUpperBound,
      Optional.ofNullable(psh.getFilterContext())
    )).flatMapMany(count -> {
      log.info("Pushing {} records", count);
      return Flux.from(sourceRecordDatabaseService.getSourceRecordFeed(
        psh.getSourceId(),
        // TODO what happens if we have two records with the same timestamp? - Unlikely but possible I guess
        //Instant.EPOCH,
        lowerBound,
        //Instant.now()
        finalUpperBound,
        Optional.ofNullable(psh.getFilterContext())
      ));
    })
    //.limitRate(3000, 2000) // Ensure we don't have massive backpressure causing enormous memory strain (?)
    // Flux<SourceRecord> -> aiming for Tuple<SourceRecord, <JsonNode>>?
    // Parallelise transform within each buffered chunk (tracking earliest and latest so order within chunk doesn't matter)
    .buffer(BUNDLE_SIZE)
    .concatMap(sourceRecordChunk -> processChunkForPushable(psh, destination, client, session, sourceRecordChunk, proteusSpec))
    .takeLast(1) // This will contain the PT as it stands after the LAST record in the stack
    .next() // This should be a passive version of last(), where if stream is empty we don't get a crash
    .flatMap(lastPsh -> {
      /* If previous footPointer is updated
       * then the between logic will return a list NOT containing
       * a record equal to or below FootPointer.
       *
       * If we have reached the end of the list successfully then the destinationHeadPointer
       * should be the new footPointer. ASSUMES THAT THE BETWEEN WORKS AS EXPECTED
       */
      // Is this if necessary? DHP should always be ahead of FP
      if (lastPsh.getDestinationHeadPointer().compareTo(psh.getFootPointer()) > 0 ) {
        // We reached the end but never moved footPointer
        lastPsh.setFootPointer(lastPsh.getDestinationHeadPointer());
        return Mono.from(pushableService.update(lastPsh));
      }

      // If nothing changed, just return PT as is
      return Mono.just(lastPsh);
      /*
       * TODO
       * DOWNSTREAM will need to inspect the changed Pushable and make decisions
       * about continuing/moving on based on pointers and head of record stack
       */
    });
  }
}
