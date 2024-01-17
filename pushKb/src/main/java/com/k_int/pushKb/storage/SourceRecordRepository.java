package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceRecord;
import com.k_int.pushKb.model.SourceType;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
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

  @Nullable
  @SingleResult
  Publisher<Instant> findMaxLastUpdatedAtSourceBySource(Source source);


  @SingleResult
	Publisher<Void> delete(UUID id);

  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);

  @NonNull
  @Join(value="source")
  Publisher<SourceRecord> findTop2OrderByCreatedDesc();

  @NonNull
  @Join(value="source")
  Publisher<SourceRecord> findAllByUpdatedBetweenOrderByUpdatedDesc(Instant footTimestamp, Instant headTimestamp);

  @NonNull
  @SingleResult
	default Publisher<SourceRecord> saveOrUpdate(@Valid @NotNull SourceRecord sr) {
    return Mono.from(this.existsById(sr.getId()))
      .flatMap( update -> {
        if (update) {
          log.info("Record with id({}) already exists, updating", sr.getId());
          return Mono.from(this.update(sr));
        }

        return Mono.from(this.save(sr));
      });
	}
}