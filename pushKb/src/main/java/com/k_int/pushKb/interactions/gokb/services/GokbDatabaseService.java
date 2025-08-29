package com.k_int.pushKb.interactions.gokb.services;

import java.util.UUID;

import com.k_int.pushKb.crud.CrudDatabaseService;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.storage.GokbRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class GokbDatabaseService implements CrudDatabaseService<Gokb> {
  private final GokbRepository gokbRepository;
	public GokbDatabaseService(
    GokbRepository gokbRepository
  ) {
    this.gokbRepository = gokbRepository;
	}

	public GokbRepository getRepository() {
		return gokbRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Gokb> ensureGokb( @Valid Gokb gokb ) {
      UUID gen_id = Gokb.generateUUIDFromGoKB(gokb);
      gokb.setId(gen_id);

      return Mono.from(gokbRepository.existsById(gen_id))
        .flatMap(doesItExist -> Mono.from(doesItExist ?
					gokbRepository.findById(gen_id) :
					gokbRepository.save(gokb)
				));
    }
}
