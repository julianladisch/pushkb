package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.storage.TemporaryPushTaskRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class TemporaryPushTaskDatabaseService implements PushableDatabaseService<TemporaryPushTask> {
  private final TemporaryPushTaskRepository temporaryPushTaskRepository;
	public TemporaryPushTaskDatabaseService(
    TemporaryPushTaskRepository temporaryPushTaskRepository
  ) {
    this.temporaryPushTaskRepository = temporaryPushTaskRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<TemporaryPushTask> findById( UUID id ) {
    return Mono.from(temporaryPushTaskRepository.findById(id));
  };

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id ) {
    return Mono.from(temporaryPushTaskRepository.existsById(id));
  };

  // Ensures that a temporary push task exists for the pushTask/filterContext combination
  @NonNull
  @SingleResult
  @Transactional
  public Publisher<TemporaryPushTask> ensurePushable( TemporaryPushTask tpt ) {
    //log.info("ensureTemporaryPushTask called with {}", tpt);
    UUID gen_id = TemporaryPushTask.generateUUIDFromTemporaryPushTask(tpt);
    tpt.setId(gen_id);

    return Mono.from(temporaryPushTaskRepository.existsById(gen_id))
      .flatMap(doesItExist -> {
        return Mono.from(doesItExist ?
          temporaryPushTaskRepository.findById(gen_id) :
          temporaryPushTaskRepository.save(tpt)
        );
    });
  };

  @Transactional
  public Publisher<TemporaryPushTask> getFeed () {
    log.info("getTemporaryPushTaskFeed");
    return temporaryPushTaskRepository.listOrderByPushTaskAndFilterContext();
  }

  @Transactional
  public Publisher<TemporaryPushTask> update (@Valid TemporaryPushTask tpt) {
    return temporaryPushTaskRepository.update(tpt);
  }

  @Transactional
  public Publisher<Boolean> complete (@Valid TemporaryPushTask tpt) {
    log.info("{}({}) completed at {}", tpt.getClass(), tpt.getId(), Instant.now());
    log.info("Deleting temporary push task.");
    return Mono.from(temporaryPushTaskRepository.delete(tpt)).map(l -> true);
  }
}
