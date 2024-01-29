package com.k_int.pushKb;

import java.time.Instant;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import com.k_int.pushKb.destinations.folio.FolioDestination;
import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.GokbSourceType;
import com.k_int.pushKb.sources.gokb.GokbSource;

public interface Boostraps {

  public static final Map<String, Source> sources = Map.ofEntries(
    new SimpleEntry<String, GokbSource>(
      "GOKB_PACKAGE",
      GokbSource.builder()
        .sourceUrl("https://gokb.org/gokb/api")
        .gokbSourceType(GokbSourceType.PACKAGE)
        .build()
    ),
    new SimpleEntry<String, GokbSource>(
      "GOKB_TIPP",
      GokbSource.builder()
        .sourceUrl("https://gokb.org/gokb/api")
        .gokbSourceType(GokbSourceType.TIPP)
        .build()
    )
  );

  public static final Map<String, ? extends Destination> destinations = Map.ofEntries(
    new SimpleEntry<String, Destination>(
      "LOCAL_RANCHER_FOLIO",
      FolioDestination.builder()
        .destinationUrl("http://localhost:8080")
        .tenant("test1")
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
  );
}
