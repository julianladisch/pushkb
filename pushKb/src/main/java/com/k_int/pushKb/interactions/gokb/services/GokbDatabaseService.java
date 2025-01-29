package com.k_int.pushKb.interactions.gokb.services;

import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.interactions.gokb.storage.GokbRepository;

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
public class GokbDatabaseService {
  private final GokbRepository gokbRepository;
	public GokbDatabaseService(
    GokbRepository gokbRepository
  ) {
    this.gokbRepository = gokbRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Gokb> findById(UUID uuid) {
    return gokbRepository.findById(uuid);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Gokb> ensureGokb( @Valid Gokb gokb ) {
      UUID gen_id = Gokb.generateUUIDFromGoKB(gokb);
      gokb.setId(gen_id);

      return Mono.from(gokbRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
            gokbRepository.findById(gen_id) :
            gokbRepository.save(gokb)
          );
      });
    };

  @NonNull
  @Transactional
  public Publisher<List<Gokb>> findAll(Pageable pageable) {
    return Mono.from(gokbRepository.findAll(pageable)).map(Page::getContent);
  }

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<Gokb> update ( @NonNull @Valid Gokb gkb ) {
    return gokbRepository.update(gkb);
  }

  @Transactional
  @SingleResult // Use when you use a Publisher representing a single result
  public Publisher<Gokb> save ( @NonNull @Valid Gokb gkb ) {
    return gokbRepository.save(gkb);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Long> deleteById( UUID id ) {
    return gokbRepository.deleteById(id);
  }
}
