package com.k_int.pushKb;

import java.time.Instant;

import com.k_int.pushKb.destinations.folio.FolioDestination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.sources.gokb.GokbSource;

public interface Boostraps {
	public static interface Sources {
		public static final GokbSource GOKB_PACKAGE = GokbSource.builder()
                                              .sourceUrl("https://gokb.org/gokb/api")
                                              .sourceType(SourceType.PACKAGE)
                                              .build();
    public static final GokbSource GOKB_TIPP = GokbSource.builder()
                                            .sourceUrl("https://gokb.org/gokb/api")
                                            .sourceType(SourceType.TIPP)
                                            .build();
	}

  public static interface Destinations {
		public static final FolioDestination LOCAL_RANCHER_FOLIO = FolioDestination.builder()
                                            .destinationUrl("http://localhost:8080")
                                            .tenant("test1")
                                            .build();
	}

  public static interface DestinationSourceLinks {
		public static final DestinationSourceLink FOLIO_GOKB_TIPP = DestinationSourceLink.builder()
                                            .transform("example_tranform")
                                            .source(Sources.GOKB_TIPP)
                                            .destination(Destinations.LOCAL_RANCHER_FOLIO)
                                            .destinationHeadPointer(Instant.EPOCH)
                                            .lastSentPointer(Instant.EPOCH)
                                            .footPointer(Instant.EPOCH)
                                            .build();
	}

  // This might be tricky now they aren't direct lookups
/*   public static interface PushTasks {
		public static final PushTask FOLIO_GOKB_TIPP = PushTask.builder()
                                            .transform("example_tranform")
                                            .sourceId(Sources.GOKB_TIPP)
                                            .destination(Destinations.LOCAL_RANCHER_FOLIO)
                                            .destinationHeadPointer(Instant.EPOCH)
                                            .lastSentPointer(Instant.EPOCH)
                                            .footPointer(Instant.EPOCH)
                                            .build();
	} */
}
