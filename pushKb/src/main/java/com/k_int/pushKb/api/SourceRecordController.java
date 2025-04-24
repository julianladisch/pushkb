package com.k_int.pushKb.api;

import java.util.UUID;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.storage.SourceRecordRepository;

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
  private final SourceRecordRepository repository;
  public SourceRecordController(SourceRecordRepository repository) {
    this.repository = repository;
  }

  // Count total records
  @Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
  public Publisher<Long> count(
    @Nullable @QueryValue UUID sourceId,
    @Nullable @QueryValue String filterContext
  ) {
    if (sourceId == null) {
      return repository.count();
    }

    if (filterContext == null) {
      return repository.countBySourceId(sourceId);
    }

    return repository.countBySourceIdAndFilterContext(sourceId, filterContext);
  }
}
