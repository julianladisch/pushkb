package com.k_int.pushKb.interactions.gokb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.storage.GokbSourceRepository;
import com.k_int.pushKb.services.SourceDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class GokbSourceDatabaseService implements SourceDatabaseService<GokbSource> {
  private final GokbSourceRepository gokbSourceRepository;
  private final GokbDatabaseService gokbDatabaseService;

	public GokbSourceDatabaseService(
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
    return Mono.from(gokbDatabaseService.ensureGokb(src.getGokb())) // Ensure the gokb first, then the source as a whole
      .flatMap(gokb -> {

        // Use the gokb that was ensured, so as not to attempt to create any gokb unnecessarily
        src.setGokb(gokb);
        return Mono.from(gokbSourceRepository.ensureSource(src));
      });
  }

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<GokbSource> saveOrUpdate ( @NonNull @Valid GokbSource src ) {
    return gokbSourceRepository.saveOrUpdate(src);
  }
}
