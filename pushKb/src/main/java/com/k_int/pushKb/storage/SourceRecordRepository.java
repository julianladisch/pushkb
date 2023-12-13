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

  @Nullable
  @SingleResult
  @Join(value="source", type = Join.Type.FETCH)
  Publisher<SourceRecord> findBySourceAndSourceUUID(@NotNull Source source, @NotNull String sourceUUID);

  @NonNull
  @SingleResult
  Publisher<Boolean> existsBySourceUUID(@NotNull String sourceUUID);

  @NonNull
  @SingleResult
	default Publisher<SourceRecord> saveOrUpdate(@Valid @NotNull SourceRecord sr) {
    return Mono.from(this.existsById(sr.getId()))
      .flatMap( update -> {
        if (update) {
          log.info("Record already exists, updating");
          return Mono.from(this.update(sr));
        }

        return Mono.from(this.save(sr));
      });
	}

  // Acts similarly to saveOrUpdate above, but works on the assumption that sourceUUID is the important factor
 	@NonNull
  @SingleResult
	default Publisher<SourceRecord> saveOrUpdateBySourceUUID(@Valid @NotNull SourceRecord sr) {
    return Mono.from(this.findBySourceAndSourceUUID(sr.getSource(), sr.getSourceUUID()))
      .flatMap( existingRecord -> {
        log.info("Record with that sourceUUID already exists, updating");
        log.info("EXISTING RECORD: {}", existingRecord);
        SourceRecord newRecord = SourceRecord.builder()
                                            .id(existingRecord.getId())
                                            .sourceUUID(sr.getSourceUUID()) // Ensure same sourceUUID
                                            .source(sr.getSource()) // Ensure same source
                                            .created(existingRecord.getCreated())
                                            .updated(existingRecord.getUpdated())
                                            .lastUpdatedAtSource(existingRecord.getLastUpdatedAtSource())
                                            .jsonRecord(existingRecord.getJsonRecord())
                                            .build();

        return Mono.from(this.saveOrUpdate(newRecord));
      })
      .switchIfEmpty(Mono.from(this.saveOrUpdate(sr)));
	}
}