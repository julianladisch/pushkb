package com.k_int.pushKb.api;

import java.util.UUID;

import com.k_int.pushKb.services.SourceRecordDatabaseService;
import io.micronaut.http.annotation.Delete;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.reactivestreams.Publisher;

import io.micronaut.http.annotation.Controller;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import lombok.extern.slf4j.Slf4j;

@Controller("/sourcerecords")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class SourceRecordController {
	private final SourceRecordDatabaseService sourceRecordDatabaseService;

  public SourceRecordController(
		SourceRecordDatabaseService sourceRecordDatabaseService
	) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
  }

  // Count total records
  @Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
  public Publisher<Long> count(
    @Nullable @QueryValue UUID sourceId,
    @Nullable @QueryValue String filterContext
  ) {
		return sourceRecordDatabaseService.countFeed(sourceId, filterContext);
  }

	@Delete(uri = "/clearRecords", produces = MediaType.APPLICATION_JSON)
	public Publisher<Long> clearRecords() {
		return sourceRecordDatabaseService.deleteAll();
	}
}
