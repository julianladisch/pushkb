package com.k_int.pushKb.interactions.gokb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.interactions.gokb.storage.GokbSourceRepository;
import com.k_int.pushKb.services.SourceDatabaseService;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
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

	public GokbSourceRepository getRepository() {
		return gokbSourceRepository;
	}

	@NonNull
	@Transactional
	public Publisher<GokbSource> list() {
		return gokbSourceRepository.list();
	}

  // FIXME is this actually used??
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<GokbSource> castToSource(JsonNode sourceObj) {
    // FIXME Only allow casting for GOKBs already created?
    return Mono.from(gokbDatabaseService.findById(UUID.fromString(sourceObj.get("gokbId").getStringValue())))
      .map(gokb -> GokbSource.builder()
				.gokbSourceType(GokbSourceType.valueOf(sourceObj.get("gokbSourceType").getStringValue()))
				.gokb(gokb)
				.name(sourceObj.get("name").getStringValue())
				.build()
			);
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
}
