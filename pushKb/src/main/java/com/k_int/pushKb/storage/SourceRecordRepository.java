package com.k_int.pushKb.storage;

import com.k_int.pushKb.crud.ReactiveStreamsPageableRepositoryUUID5;
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
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Singleton
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface SourceRecordRepository extends ReactiveStreamsPageableRepositoryUUID5<SourceRecord> {
  Logger log = org.slf4j.LoggerFactory.getLogger(SourceRecordRepository.class);

  @Nullable
  @SingleResult
  Publisher<Instant> findMaxUpdatedBySourceId(UUID sourceId);

  @Nullable
  @SingleResult
  Publisher<Instant> findMaxUpdatedBySourceIdAndFilterContext(UUID sourceId, String context);

  @Transactional
  @SingleResult
	Publisher<Void> delete(UUID id);

  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);

  // Between is inclusive of end
/*   @NonNull
  @Join(value="source")
  Publisher<SourceRecord> findAllBySourceAndUpdatedBetweenOrderByUpdatedDescAndIdAsc(Source source, Instant footTimestamp, Instant headTimestamp);
 */

  @NonNull
  @SingleResult
  Publisher<Long> countBySourceIdAndFilterContext(
    UUID sourceId,
    String context
  );

  @NonNull
  @SingleResult
  Publisher<Long> countBySourceId(
    UUID sourceId
  );

  @NonNull
  @SingleResult
  Publisher<Long> countBySourceIdAndUpdatedGreaterThanAndUpdatedLessThan(
    UUID sourceId,
    Instant footTimestamp,
    Instant headTimestamp
  );

  @NonNull
  @SingleResult
  Publisher<Long> countBySourceIdAndFilterContextAndUpdatedGreaterThanAndUpdatedLessThan(
    UUID sourceId,
    String context,
    Instant footTimestamp,
    Instant headTimestamp
  );

  @NonNull
  Publisher<SourceRecord> findTop1000BySourceIdAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(
    UUID sourceId,
    Instant footTimestamp,
    Instant headTimestamp
  );

  @NonNull
  Publisher<SourceRecord> findTop1000BySourceIdAndFilterContextAndUpdatedGreaterThanAndUpdatedLessThanOrderByUpdatedDescAndIdAsc(
    UUID sourceId,
    String context,
    Instant footTimestamp,
    Instant headTimestamp
  );

  @NonNull
  Publisher<SourceRecord> findBySourceIdAndUpdatedOrderByUpdatedDescAndIdAsc(
    UUID sourceId,
    Instant updated
  );


  @NonNull
  Publisher<SourceRecord> findBySourceIdAndFilterContextAndUpdatedOrderByUpdatedDescAndIdAsc(
    UUID sourceId,
    String context,
    Instant updated
  );

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

  @Transactional
  @NonNull
  @SingleResult
	default Publisher<SourceRecord> saveOrUpdate(@Valid @NotNull SourceRecord sr) {
    return Mono.from(this.existsById(sr.getId()))
      .flatMap( update -> {
        if (Boolean.TRUE.equals(update)) {
          log.info("Record with id({}) already exists, updating", sr.getId());
          return Mono.from(this.update(sr));
        }

        return Mono.from(this.save(sr));
      });
	}

  @Override
  default UUID generateUUIDFromObject(SourceRecord obj) {
    return SourceRecord.generateUUIDFromSourceRecord(obj);
  }
}
