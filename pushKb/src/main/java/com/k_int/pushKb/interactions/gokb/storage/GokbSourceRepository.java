package com.k_int.pushKb.interactions.gokb.storage;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Singleton
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
  @Override
  @NonNull
  @SingleResult
	@Join("gokb")
  Publisher<GokbSource> findById(@Nullable UUID id);

  @Override
  @NonNull
  @SingleResult
  @Transactional
	@Join("gokb")
  Publisher<GokbSource> save(@Valid @NotNull GokbSource src);

  @Override
  @NonNull
  @SingleResult
  @Transactional
	@Join("gokb")
  Publisher<GokbSource> update(@Valid @NotNull GokbSource src);

  @Override
  @NonNull
  @Join("gokb")
  Publisher<GokbSource> list();

  @Override
  @NonNull
  @Join("gokb")
  Publisher<Page<GokbSource>> findAll(Pageable pageable);

  @Override // I don't love that this has to be overwritten in every repository.
  default UUID generateUUIDFromObject(GokbSource obj) {
    return GokbSource.generateUUIDFromSource(obj);
  }
}
