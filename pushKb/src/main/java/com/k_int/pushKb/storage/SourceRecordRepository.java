package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.SourceRecord;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
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

  @Nullable
  @SingleResult
  Publisher<Instant> findMaxLastUpdatedAtSourceBySourceId(UUID sourceId);

  @Nullable
  @SingleResult
  Publisher<Instant> findMaxUpdatedBySourceId(UUID sourceId);

  @SingleResult
	Publisher<Void> delete(UUID id);

  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);

  @NonNull
  Publisher<SourceRecord> findTop2OrderByCreatedDesc();

  // Between is inclusive of end
/*   @NonNull
  @Join(value="source")
  Publisher<SourceRecord> findAllBySourceAndUpdatedBetweenOrderByUpdatedDescAndIdAsc(Source source, Instant footTimestamp, Instant headTimestamp);
 */

  @NonNull
  Publisher<SourceRecord> findAllBySourceIdAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(UUID sourceId, Instant footTimestamp, Instant headTimestamp);

  // Finds values STRICTLY between foot and head timestamps
  // Having to manually input these fields is a dealbreaker for me, using automagical Query above instead

/* @Join(value = "source", alias="s_")
  @Query(value = """
    SELECT
      sr.*,
      s_.code AS s_code,
      s_.source_url as s_source_url,
      s_.source_type as s_source_type
    FROM source_record AS sr
    LEFT JOIN source AS s_ ON s_.id = sr.source_id
      WHERE
        sr.source_id = :source AND
        sr.updated > :footTimestamp AND
        sr.updated < :headTimestamp
    ORDER BY sr.updated DESC, sr.id ASC;
    """, nativeQuery = true)
	Publisher<SourceRecord> getSourceRecordFeedBySource(Source source, Instant footTimestamp, Instant headTimestamp); */


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