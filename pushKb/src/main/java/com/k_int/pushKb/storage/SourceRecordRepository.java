package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.SourceRecord;

import java.util.UUID;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface SourceRecordRepository extends ReactiveStreamsPageableRepository<SourceRecord, UUID> {
  Logger log = org.slf4j.LoggerFactory.getLogger(SourceRecordRepository.class);

  @SingleResult
	@NonNull
	default Publisher<SourceRecord> saveOrUpdate(@Valid @NotNull SourceRecord sr) {
		return Mono.from(this.existsById(sr.getId()))
			.flatMap( update -> {
        if (update) {
          // FIXME In the actual service we should remove and replace at front of queue 
          log.info("Record already exists, updating");
          return Mono.from(this.update(sr));
        }

        return Mono.from(this.save(sr));
      });
	}
}