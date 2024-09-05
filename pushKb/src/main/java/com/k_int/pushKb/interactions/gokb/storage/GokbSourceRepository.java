package com.k_int.pushKb.interactions.gokb.storage;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface GokbSourceRepository extends SourceRepository<GokbSource> {
  // Specific Gokb ensureSource (Needs generateUUID cos we've decided to use UUID5)
  @NonNull
  @SingleResult
  @Transactional
	@Join("gokb")
  default Publisher<GokbSource> ensureSource( GokbSource src ) {
    UUID gen_id = GokbSource.generateUUID(
      src.getGokb(),
      src.getGokbSourceType()
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

  // Unique up to baseUrl
  @NonNull
  @SingleResult
	@Join("gokb")
  Publisher<GokbSource> findById(@Nullable UUID id);

  @NonNull
  @SingleResult
	@Join("gokb")
  Publisher<GokbSource> save(@Valid @NotNull GokbSource src);

  @NonNull
  @Transactional
  @Join("gokb")
  Publisher<GokbSource> list();
}
