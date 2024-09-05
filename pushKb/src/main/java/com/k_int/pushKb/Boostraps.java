package com.k_int.pushKb;

import java.time.Instant;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioDestinationType;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
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
      "LOCAL_RANCHER_FOLIO_PACKAGE",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.OKAPI)
            .tenant("test1")
            .baseUrl("http://localhost:30100")
            .loginUser(System.getenv("LOCAL_RANCHER_USERNAME"))
            .loginPassword(System.getenv("LOCAL_RANCHER_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PACKAGE)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "LOCAL_RANCHER_FOLIO_PCI",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.OKAPI)
            .tenant("test1")
            .baseUrl("http://localhost:30100")
            .loginUser(System.getenv("LOCAL_RANCHER_USERNAME"))
            .loginPassword(System.getenv("LOCAL_RANCHER_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PCI)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "LOCAL_DC_FOLIO_PACKAGE",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("test1")
            .baseUrl("http://localhost:8080")
            .build()
        )
        .destinationType(FolioDestinationType.PACKAGE)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "LOCAL_DC_FOLIO_PCI",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("test1")
            .baseUrl("http://localhost:8080")
            .build()
        )
        .destinationType(FolioDestinationType.PCI)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT_PACKAGE",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("diku")
            .baseUrl("https://folio-snapshot-okapi.dev.folio.org")
            .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
            .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PACKAGE)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT_PCI",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("diku")
            .baseUrl("https://folio-snapshot-okapi.dev.folio.org")
            .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
            .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PCI)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT2_PACKAGE",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("diku")
            .baseUrl("https://folio-snapshot-2-okapi.dev.folio.org")
            .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
            .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PACKAGE)
        // Use env vars for now... this file will eventually be gone anyway
        .build()
    ),
    new SimpleEntry<String, Destination>(
      "SNAPSHOT2_PCI",
      FolioDestination.builder()
        .folioTenant(
          FolioTenant.builder()
            .authType(FolioAuthType.NONE)
            .tenant("diku")
            .baseUrl("https://folio-snapshot-2-okapi.dev.folio.org")
            .loginUser(System.getenv("FOLIO_SNAPSHOT_USERNAME"))
            .loginPassword(System.getenv("FOLIO_SNAPSHOT_PASSWORD"))
            .build()
        )
        .destinationType(FolioDestinationType.PCI)
        // Use env vars for now... this file will eventually be gone anyway
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
          (FolioDestination) destinations.get("LOCAL_DC_FOLIO_PCI")
        ))
        .destinationType(FolioDestination.class)
        .destinationHeadPointer(Instant.EPOCH)
        .lastSentPointer(Instant.EPOCH)
        .footPointer(Instant.EPOCH)
        .build()
    )
  );
}
