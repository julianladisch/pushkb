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
import reactor.core.publisher.Mono;

@Singleton
public class PushTaskService {
  private final PushTaskRepository pushTaskRepository;
	public PushTaskService(
    PushTaskRepository pushTaskRepository
  ) {
    this.pushTaskRepository = pushTaskRepository;
	}

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<PushTask> ensurePushTask( PushTask pt ) {
    // This will ensure a PushTask from the given data, but does NOT make sure that the Source/Destination exist first
    /* TODO we could error out if the Destination/Source don't exist by using existsById
     * on destinationService/sourceService, combined with the stored Class information
     */
      UUID gen_id = PushTask.generateUUIDFromPushTask(pt);
      pt.setId(gen_id);

      return Mono.from(pushTaskRepository.existsById(gen_id))
        .flatMap(doesItExist -> {
          return Mono.from(doesItExist ?
          pushTaskRepository.findById(gen_id) :
          pushTaskRepository.save(pt)
          );
      });
    };

  // FIXME Not sure if raw feed is the way to go here, but let's get it working first
  @Transactional
  protected Publisher<PushTask> getPushTaskFeed () {
    return pushTaskRepository.listOrderBySourceIdAndDestinationIdAndId();
  }
}
