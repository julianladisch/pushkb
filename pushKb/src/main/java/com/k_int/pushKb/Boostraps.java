package com.k_int.pushKb;

import java.time.Instant;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.Source;

public interface Boostraps {
  public static final Map<String, ? extends Source> sources = Map.ofEntries(
    new SimpleEntry<String, GokbSource>(
      "GOKB_PACKAGE",
      GokbSource.builder()
        .gokb(
          Gokb.builder()
            .baseUrl("https://gokb.org")
            .build()
        )
        .gokbSourceType(GokbSourceType.PACKAGE)
        .build()
    ),
    new SimpleEntry<String, GokbSource>(
      "GOKB_TIPP",
      GokbSource.builder()
        .gokb(
          Gokb.builder()
            .baseUrl("https://gokb.org")
            .build()
        )
        .gokbSourceType(GokbSourceType.TIPP)
        .build()
    )
/*     new SimpleEntry<String, GokbSource>(
      "GOKBT_PACKAGE",
      GokbSource.builder()
        .gokb(
          Gokb.builder()
            .baseUrl("https://gokbt.gbv.de")
            .build()
        )
        .gokbSourceType(GokbSourceType.PACKAGE)
        .build()
    ),
    new SimpleEntry<String, GokbSource>(
      "GOKBT_TIPP",
      GokbSource.builder()
        .gokb(
          Gokb.builder()
            .baseUrl("https://gokbt.gbv.de")
            .build()
        )
        .gokbSourceType(GokbSourceType.TIPP)
        .build()
    ) */
  );

  public static final Map<String, ? extends Destination> destinations = Map.ofEntries(
    new SimpleEntry<String, Destination>(
      "LOCAL_RANCHER_FOLIO",
      FolioDestination.builder()
        .destinationUrl("http://localhost:30100")
        .tenant("test1")
        // Use env vars for now... this file will eventually be gone anyway
        .loginUser(System.getenv("LOCAL_RANCHER_USERNAME"))
        .loginPassword(System.getenv("LOCAL_RANCHER_PASSWORD"))
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT",
      FolioDestination.builder()
        .destinationUrl("https://folio-snapshot-okapi.dev.folio.org")
        .tenant("diku")
        // LOGIN DETAILS NEED CHANGING IN DB AFTER BOOTSTRAPPING
        .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
        .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT2",
      FolioDestination.builder()
        .destinationUrl("https://folio-snapshot-2-okapi.dev.folio.org")
        .tenant("diku")
        // LOGIN DETAILS NEED CHANGING IN DB AFTER BOOTSTRAPPING
        .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
        .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
        .build()
    )
  );

  public static final Map<String, PushTask> pushTasks = Map.ofEntries(
    new SimpleEntry<String, PushTask>(
      "FOLIO_GOKB_TIPP",
      PushTask.builder()
        .transform("example_tranform")
        .sourceId(GokbSource.generateUUIDFromSource(
          (GokbSource) sources.get("GOKB_TIPP")
        ))
        .sourceType(GokbSource.class)
        .destinationId(FolioDestination.generateUUIDFromDestination(
          (FolioDestination) destinations.get("LOCAL_RANCHER_FOLIO")
        ))
        .destinationType(FolioDestination.class)
        .destinationHeadPointer(Instant.EPOCH)
        .lastSentPointer(Instant.EPOCH)
        .footPointer(Instant.EPOCH)
        .build()
    )
    /* new SimpleEntry<String, PushTask>(
      "FOLIO_GOKBT_TIPP",
      PushTask.builder()
        .transform("example_tranform")
        .sourceId(GokbSource.generateUUIDFromSource(
          (GokbSource) sources.get("GOKBT_TIPP")
        ))
        .sourceType(GokbSource.class)
        .destinationId(FolioDestination.generateUUIDFromDestination(
          (FolioDestination) destinations.get("LOCAL_RANCHER_FOLIO")
        ))
        .destinationType(FolioDestination.class)
        .destinationHeadPointer(Instant.EPOCH)
        .lastSentPointer(Instant.EPOCH)
        .footPointer(Instant.EPOCH)
        .build()
    ) */
  );
}
