package com.k_int.pushKb;

import java.time.Instant;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.DestinationType;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;

public interface Boostraps {
	public static interface Sources {
		public static final Source GOKB_PACKAGE = Source.builder()
                                            .sourceUrl("https://gokb.org/gokb/api")
                                            .code(SourceCode.GOKB)
                                            .sourceType(SourceType.PACKAGE)
                                            .build();
    public static final Source GOKB_TIPP = Source.builder()
                                            .sourceUrl("https://gokb.org/gokb/api")
                                            .code(SourceCode.GOKB)
                                            .sourceType(SourceType.TIPP)
                                            .build();
	}

  public static interface Destinations {
		public static final Destination FOLIO = Destination.builder()
                                            .destinationUrl("https://test.com")
                                            .destinationType(DestinationType.FOLIO)
                                            .build();
	}

  public static interface DestinationSourceLinks {
		public static final DestinationSourceLink FOLIO_GOKB_TIPP = DestinationSourceLink.builder()
                                            .transform("example_tranform")
                                            .source(Sources.GOKB_TIPP)
                                            .destination(Destinations.FOLIO)
                                            .destinationHeadPointer(Instant.EPOCH)
                                            .lastSentPointer(Instant.EPOCH)
                                            .footPointer(Instant.EPOCH)
                                            .build();
	}
}
