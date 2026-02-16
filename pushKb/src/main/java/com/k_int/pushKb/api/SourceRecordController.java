package com.k_int.pushKb.api;

import java.util.UUID;

import com.k_int.pushKb.services.SourceRecordDatabaseService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Controller("/sourcerecords")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class SourceRecordController implements SourceRecordApi {
	private final SourceRecordDatabaseService sourceRecordDatabaseService;

  public SourceRecordController(
		SourceRecordDatabaseService sourceRecordDatabaseService
	) {
		this.sourceRecordDatabaseService = sourceRecordDatabaseService;
  }

  // Count total records
	@Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
	public Mono<Long> count(
    @Nullable @QueryValue UUID sourceId,
    @Nullable @QueryValue String filterContext
  ) {
		return Mono.from(sourceRecordDatabaseService.countFeed(sourceId, filterContext));
  }

	@Delete(uri = "/clearRecords", produces = MediaType.APPLICATION_JSON)
	@Status(HttpStatus.NO_CONTENT)
	public Mono<Void> clearRecords() {
		return Mono.from(sourceRecordDatabaseService.deleteAll()).then();
		// TODO should this reset the pointers as a side effect?
	}
}
