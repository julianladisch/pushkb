package com.k_int.pushKb.services;

import java.time.Instant;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.storage.PushTaskRepository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class PushTaskDatabaseService implements PushableDatabaseService<PushTask> {
  private final PushTaskRepository pushTaskRepository;

	public PushTaskDatabaseService(
    PushTaskRepository pushTaskRepository
  ) {
    this.pushTaskRepository = pushTaskRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( UUID id ) {
    return Mono.from(pushTaskRepository.existsById(id));
  };

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<PushTask> findById( UUID id ) {
    return Mono.from(pushTaskRepository.findById(id));
  };


  @NonNull
  @SingleResult
  @Transactional
  public Publisher<PushTask> ensurePushable( PushTask pt ) {
    //log.info("ensurePushTask called with {}", pt);
    // This will ensure a PushTask from the given data, but does NOT make sure that the Source/Destination exist first
    /* TODO we could error out if the Destination/Source don't exist by using existsById
     * on destinationService/sourceService, combined with the stored Class information
     */
    UUID gen_id = PushTask.generateUUIDFromPushTask(pt);
    pt.setId(gen_id);

    return Mono.from(pushTaskRepository.existsById(gen_id))
      .flatMap(doesItExist -> {
        if (doesItExist) {
          return Mono.from(pushTaskRepository.findById(gen_id));
        }

        return Mono.from(pushTaskRepository.save(pt));
      });
  };


  // FIXME Not sure if raw feeds are the way to go here, but let's get it working first
  @Transactional
  public Publisher<PushTask> getFeed () {
    log.info("getPushTaskFeed");
    return pushTaskRepository.listOrderBySourceIdAndDestinationIdAndId();
  }

  @Transactional
  public Publisher<PushTask> update (@Valid PushTask pt) {
    return pushTaskRepository.update(pt);
  }

  @Transactional
  public Publisher<Boolean> complete (@Valid PushTask pt) {
    log.info("{}({}) completed at {}", pt.getClass(), pt.getId(), Instant.now());
    return Mono.just(true);
  }
}
