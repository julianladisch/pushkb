package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;

import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class SourceService {
  private final SourceRepository sourceRepository;
	public SourceService(
    SourceRepository sourceRepository
  ) {
    this.sourceRepository = sourceRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Source> findById( UUID id ) {
    return sourceRepository.findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Source> ensureSource( String sourceUrl, SourceCode code, SourceType type ) {
    Source src = Source.builder()
                       .sourceUrl(sourceUrl)
                       .code(code)
                       .sourceType(type)
                       .build();

    return ensureSource(src);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Source> ensureSource( Source src ) {
    UUID gen_id = Source.generateUUID(
      src.getCode(),
      src.getSourceType(),
      src.getSourceUrl()
    );

    // Set up new Source so we're definitely not passing the one from the parameters
    Source new_source = src.toBuilder()
                           .id(gen_id)
                           .build();

    return Mono.from(sourceRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            sourceRepository.findById(gen_id) :
            sourceRepository.save(new_source)
          );
        });
  }
}
