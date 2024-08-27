package com.k_int.pushKb.interactions.gokb.source;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.GokbDatabaseService;
import com.k_int.pushKb.services.SourceDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class GokbSourceService implements SourceDatabaseService<GokbSource> {
  private final GokbSourceRepository gokbSourceRepository;
  private final GokbDatabaseService gokbDatabaseService;

	public GokbSourceService(
    GokbSourceRepository gokbSourceRepository,
    GokbDatabaseService gokbDatabaseService
  ) {
		this.gokbSourceRepository = gokbSourceRepository;
    this.gokbDatabaseService = gokbDatabaseService;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<GokbSource> findById(UUID id ) {
    return gokbSourceRepository.findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id ) {
    return gokbSourceRepository.existsById(id);
  }

  @NonNull
  @Transactional
  public Publisher<GokbSource> list() {
    return gokbSourceRepository.list();
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<GokbSource> ensureSource( GokbSource src ) {
    return Mono.from(gokbDatabaseService.ensureGokb(src.gokb)) // Ensure the gokb first, then the source as a whole
      .flatMap(gokb -> {

        // Use the gokb that was ensured, so as not to attempt to create any gokb unnecessarily
        src.gokb = gokb;
        return Mono.from(gokbSourceRepository.ensureSource(src));
      });
  }
}
