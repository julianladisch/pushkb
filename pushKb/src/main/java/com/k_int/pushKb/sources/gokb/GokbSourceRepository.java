package com.k_int.pushKb.sources.gokb;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.GokbSourceType;
import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface GokbSourceRepository extends SourceRepository<GokbSource>{
  // FIXME a lot of these aren't used yet
  @NonNull
	Publisher<GokbSource> findAllBySourceUrl ( String sourceUrl );

  @NonNull
  @SingleResult
	Publisher<GokbSource> findBySourceUrlAndGokbSourceType ( String sourceUrl, GokbSourceType type );

  // Find by relevant data, even from built Source without id.
  @NonNull
  @SingleResult
	default Publisher<GokbSource> findBySourceData ( GokbSource source ) {
    return findBySourceUrlAndGokbSourceType(source.getSourceUrl(), source.getGokbSourceType());
  }

  @NonNull
  @SingleResult
	Publisher<Boolean> existsBySourceUrlAndGokbSourceType ( String sourceUrl, GokbSourceType type );

  @NonNull
  @SingleResult
	default Publisher<Boolean> existsBySourceData ( GokbSource source ) {
    return existsBySourceUrlAndGokbSourceType(source.getSourceUrl(), source.getGokbSourceType());
  }

  @NonNull
	Publisher<GokbSource> findAllByGokbSourceType ( GokbSourceType type );

  // Specific Gokb ensureSource (Needs generateUUID cos we've decided to use UUID5)
  @NonNull
  @SingleResult
  @Transactional
  default Publisher<GokbSource> ensureSource( GokbSource src ) {
    UUID gen_id = GokbSource.generateUUID(
      src.getGokbSourceType(),
      src.getSourceUrl()
    );

    src.setId(gen_id);

    return Mono.from(existsById(gen_id))
        .flatMap(doesItExist -> {
          if (doesItExist) {
            return Mono.from(findById(gen_id));
          }

          return Mono.from(save(src));
        });
  }
}
