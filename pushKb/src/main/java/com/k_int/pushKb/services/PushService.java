package com.k_int.pushKb.services;

import java.beans.Transient;
import java.io.IOException;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.k_int.proteus.ComponentSpec;
import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushChunk;
import com.k_int.pushKb.model.PushSession;
import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.proteus.ProteusService;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.transaction.TransactionDefinition.Propagation;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;
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
	private final ObjectMapper objectMapper;

	public PushService(
		SourceRecordDatabaseService sourceRecordDatabaseService,
    PushableService pushableService,
		ProteusService proteusService,
    DestinationService destinationService,
    PushSessionDatabaseService pushSessionDatabaseService,
    PushChunkDatabaseService pushChunkDatabaseService,
    ObjectMapper objectMapper
	) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
    this.pushableService = pushableService;
    this.pushSessionDatabaseService = pushSessionDatabaseService;
    this.pushChunkDatabaseService = pushChunkDatabaseService;
		this.proteusService = proteusService;
    this.destinationService = destinationService;
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


  protected Mono<PushChunk> setUpPushChunk(
    PushSession session
  ) {
    return Mono.from(pushChunkDatabaseService.save(
      PushChunk.builder()
        .session(session)
        .build()
    ));
  }

  // Process 1000 records and send to target
  // RETURNS earliestSeen, latestSeen and remainingCount (naive)
  protected Mono<Tuple3<Instant, Instant, Long>> processAndPushRecords(
    Pushable psh,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session,
    List<SourceRecord> sourceRecordChunk,
    PushChunk chunk,
    ComponentSpec<JsonNode> proteusSpec
  ) {
    // TODO this might be a bit noisy in general
    log.info("PushService::processAndPushRecords called with {} records", sourceRecordChunk.size());
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
          e.printStackTrace();
          return Mono.error(e);
        }
      })
      .collectList()
      // Change List<Tuple2<SourceRecord, JsonNode>> into Tuple3<List<JsonNode>, Instant, Instant>
      // where instants are earliest and latest timestamp in chunk
      .flatMap(list -> {
        //log.info("Handled list of 1000 {}", list.size());
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
        return Mono.from(destinationService.push(destination, client, json)).flatMap(bool -> {
          // bool should just be true, we're only interested in outputting earliestSeen/latestSeen

          // Get a grasp on remainingCount
          return Mono.from(
            sourceRecordDatabaseService.countFeed(
            psh.getSourceId(),
            psh.getFootPointer(),
            earliestSeen,
            Optional.ofNullable(psh.getFilterContext())
          )).flatMap(remainingCount -> {
            return Mono.just(Tuples.of(earliestSeen, latestSeen, remainingCount));
          });
        });

        //log.info("SENT RECORD: {}", sr.getId());
      }));
  }

  // This is useable from the point of view of TemporaryPushTasks AND PushTasks
  public Mono<Tuple5<Source, Destination, DestinationClient<Destination>, PushSession, ComponentSpec<JsonNode>>> getPushableTuple(Pushable p) {
    return Mono.from(pushableService.getSourceAndDestination(p))
    .flatMap(TupleUtils.function((source, destination) -> {
      // FIXME this needs to come from the Pushable transform model somehow
      // FIXME but for now we'll do a direct (domain specific) switch on GokbSourceType (DO NOT DO THIS IN FINAL VERSION)
      ComponentSpec<JsonNode> proteusSpec = proteusService.loadSpec(
        GokbSource.class.cast(source).getGokbSourceType() == GokbSourceType.TIPP ?
        "GOKBScroll_TIPP_ERM_transformV1.json" :
        "GOKBScroll_PKG_ERM_transformV1.json"
      );

      return Mono.from(destinationService.getClient(destination))
        .flatMap(client -> {
          return Mono.from(pushSessionDatabaseService.save(
            PushSession.builder()
              .pushableId(p.getId())
              .pushableType(p.getClass())
              .build()
          ))
          .flatMap(pushSession -> {
            return Mono.just(Tuples.of(source, destination, client, pushSession, proteusSpec));
          });
        });
    }));
  } // Nested flatMaps isn't very pretty here, but it brings them altogether without multiple Tuple setups and reads

  public Mono<Pushable> runPushable(Pushable p) {
    return getPushableTuple(p)
      .flatMap(TupleUtils.function((source, destination, client, session, proteusSpec) -> runPushableRecursive(p, source, destination, client, session, proteusSpec)));
  }

  public Mono<List<SourceRecord>> get1000SourceRecords(Pushable psh, Instant upperBound, Instant lowerBound) {
    return Flux.from(sourceRecordDatabaseService.getSourceRecordFeed(
      psh.getSourceId(),
      // If we have two records with the same timestamp we miss records.
      // This is actually quite common, so there's an "overlap" resend after a push.
      // This means the destination WILL get some files twice, and it's expected that they will handle this
      //Instant.EPOCH,
      lowerBound,
      //Instant.now()
      upperBound,
      Optional.ofNullable(psh.getFilterContext())
    )).collectList();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW) // Set in its own transaction so it can rollback all together if anything fails
  public Mono<Pushable> processChunk(
    Pushable psh,
    Instant upperBound,
    Instant lowerBound,
    Destination destination,
    Source source,
    DestinationClient<Destination> client,
    PushChunk chunk,
    PushSession session,
    ComponentSpec<JsonNode> proteusSpec,
    Long initialCount
  ) {
    return get1000SourceRecords(psh, upperBound, lowerBound)
      .flatMap(sourceRecordChunk -> {
        return processAndPushRecords(psh, destination, client, session, sourceRecordChunk, chunk, proteusSpec)
          // ----- CATCH UP IF WE MISSED ANY SOURCE RECORDS -----
          // This is the only place we can't recover from a dropped instance -- we'd be missing some records
          // TODO If we handle the transaction boundaries here better we could rollback all the saves in one go?
          .flatMap(TupleUtils.function((earliestSeen, latestSeen, remainingCount1) -> {
            // First things first, let's check whether we're potentially skipping any records
              log.info("{} records remaining in this queue", remainingCount1);
              Long expectedCount = Long.max(initialCount - 1000L, 0L);
              if (!remainingCount1.equals(expectedCount)) {
                log.warn("We're potentially missing records (remainingCount: {}, expectedCount: {}), specifically fetch and send those.", remainingCount1, expectedCount);

                // We want to process and send a much smaller chunk here by itself
                return getCatchUpSourceRecords(psh, earliestSeen).flatMap(catchUpChunk -> {
                  return processAndPushRecords(psh, destination, client, session, catchUpChunk, chunk, proteusSpec);
                });
              }

              // If no catch up is needed, just send down the earliestSeen/latestSeen again
              return Mono.just(Tuples.of(earliestSeen, latestSeen, remainingCount1));
          }));
    })
    .flatMap(TupleUtils.function((earliestSeen, latestSeen, remainingCount2) -> {
      // Should just be "true", idk why this would ever be false tbh

      // The earliest record "successfully sent" was the "last sent" timestamp, as we're moving _down_ the list
      psh.setLastSentPointer(earliestSeen);

      // Now compare the latest updated record sent in this chunk to the DHP
      if (latestSeen.compareTo(psh.getDestinationHeadPointer()) > 0) {
        // We have a new head pointer
        psh.setDestinationHeadPointer(latestSeen);
      }

      if (remainingCount2 == 0) {
        // We have no more records to send between lastSentPointer and footPointer,
        // bring footPointer up to destination head pointer
        psh.setFootPointer(psh.getDestinationHeadPointer());
      }
      return Mono.from(pushableService.update(psh));
    }));
  }

  // Fetch those records which specifically match a SINGLE updated timestamp.
  // I'm betting that there'll never be enough of these to cause memory issues -- this might be foolish
  public Mono<List<SourceRecord>> getCatchUpSourceRecords(Pushable psh, Instant exactUpdated) {
    return Flux.from(sourceRecordDatabaseService.getSourceRecordFeedForUpdated(
      psh.getSourceId(),
      exactUpdated,
      Optional.ofNullable(psh.getFilterContext())
    )).collectList();
  }

  // This method will do a SINGLE chunk of 1000 and then call itself
  private Mono<Pushable> runPushableRecursive(
    Pushable psh,
    Source source,
    Destination destination,
    DestinationClient<Destination> client,
    PushSession session,
    ComponentSpec<JsonNode> proteusSpec
  ) {
    final Instant lowerBound = psh.getFootPointer();
    Instant upperBound = Instant.now();

    if (psh.getDestinationHeadPointer().compareTo(psh.getFootPointer()) != 0) {
      // If the DHP is NOT equal to FP, then we are in the "gap", continue from LSP
      upperBound = psh.getLastSentPointer();
    } // Otherwise, use head of stack with Instant.now()
    final Instant finalUpperBound = upperBound;

    log.info("UPPER BOUND: {}", finalUpperBound);
    log.info("LOWER BOUND: {}", lowerBound);

    // The plan here is to first check if there are "zip up" records.
    // If so, fetch 1k of them, process, send and then recurse this method.
    // If not, run from head of records queue downwards fetching 1k and then recurse this method.

    // Should mean we always do full ingest even if it's ingesting while we push.
    // Also keeps each transaction down to 1k in from DB, process/send and then update Pushable.
    // Hopefully 30s-1m at most.
    // This will ALSO prevent loading all ~1.1m records into memory at once.
    // It _will_ still have a lock on the DutyCycleTask for the whole duration,
    // to be checked whether that causes any postgres issues
    return Mono.from(sourceRecordDatabaseService.countFeed(
        psh.getSourceId(),
        lowerBound,
        finalUpperBound,
        Optional.ofNullable(psh.getFilterContext())
      )).flatMap(initialCount -> {
        if (initialCount != 0) {
          log.info("PushService::runPushableRecursive with {} records in queue", initialCount);
          return Mono.from(setUpPushChunk(session))
            // Separated this part of the stream out ONLY so I can wrap it in a single transaction we can roll back
            .flatMap(chunk -> processChunk(psh, finalUpperBound, lowerBound, destination, source, client, chunk, session, proteusSpec, initialCount))
            .flatMap(persistedPsh -> {
              return runPushableRecursive(persistedPsh, source, destination, client, session, proteusSpec);
            });
        }

        log.info("PushService::runPushableRecursive has reached end of queue");
        return Mono.just(psh);
      });
  }
}
