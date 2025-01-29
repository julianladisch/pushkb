package com.k_int.pushKb.interactions.gokb.services;

import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.interactions.gokb.storage.GokbSourceRepository;
import com.k_int.pushKb.services.SourceDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.json.tree.JsonNode;
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
  @SingleResult
  @Transactional
  public Publisher<Long> deleteById( UUID id ) {
    return gokbSourceRepository.deleteById(id);
  }

  @NonNull
  @Transactional
  public Publisher<GokbSource> list() {
    return gokbSourceRepository.list();
  }

  @NonNull
  @Transactional
  public Publisher<List<GokbSource>> findAll(Pageable pageable) {
    return Mono.from(gokbSourceRepository.findAll(pageable)).map(Page::getContent);
  }

  // FIXME is this actually used??
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<GokbSource> castToSource(JsonNode sourceObj) {
    // FIXME Only allow casting for GOKBs already created?
    return Mono.from(gokbDatabaseService.findById(UUID.fromString(sourceObj.get("gokbId").getStringValue())))
      .map(gokb -> {
        return GokbSource.builder()
        .gokbSourceType(GokbSourceType.valueOf(sourceObj.get("gokbSourceType").getStringValue()))
        .gokb(gokb)
        .name(sourceObj.get("name").getStringValue())
        .build();
      });
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<GokbSource> ensureSource( @Valid GokbSource src ) {
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

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<GokbSource> update ( @NonNull @Valid GokbSource src ) {
    return gokbSourceRepository.update(src);
  }

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<GokbSource> save ( @NonNull @Valid GokbSource src ) {
    return gokbSourceRepository.save(src);
  }
}
