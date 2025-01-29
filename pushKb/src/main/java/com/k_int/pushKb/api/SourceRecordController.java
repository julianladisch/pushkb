package com.k_int.pushKb.api;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.storage.SourceRecordRepository;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import lombok.extern.slf4j.Slf4j;

// FIXME This should be Auth protected
@Controller("/sourcerecords")
@Slf4j
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
