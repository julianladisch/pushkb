package com.k_int.pushKb.services;

import java.util.Map;
import java.util.UUID;

import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import io.micronaut.context.ApplicationContext;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.model.Source;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Singleton
@Slf4j
public class PushableService {
  private final BeanContext beanContext;
  private final DestinationService destinationService;
  private final SourceService sourceService;

	private final ApplicationContext applicationContext;

	private final ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner;
	private final ReactiveDutyCycleTaskRepository reactiveDutyCycleTaskRepository; // Should this be handled by the runner?

  public PushableService (
    BeanContext beanContext,
    DestinationService destinationService,
    SourceService sourceService,
    ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner,
		ReactiveDutyCycleTaskRepository reactiveDutyCycleTaskRepository,
		ApplicationContext applicationContext
	) {
    this.beanContext = beanContext;
    this.destinationService = destinationService;
    this.sourceService = sourceService;
    this.reactiveDutyCycleTaskRunner = reactiveDutyCycleTaskRunner;
		this.reactiveDutyCycleTaskRepository = reactiveDutyCycleTaskRepository;
		this.applicationContext = applicationContext;
	}

  // Fast way to register pushables directly
  public Mono<DutyCycleTask> registerPushableTask(Class<? extends Pushable> type, Pushable p) {
    String pushableId = p.getId().toString();
    return reactiveDutyCycleTaskRunner.registerTask(
			(long) (1000 * 60 * 60),
      pushableId,
      "PushableScheduledTask",
      Map.of(
        "pushable", pushableId,
        "pushable_class", type.getName()
      )
    );
  }

	@SuppressWarnings("unchecked")
	public <T extends Pushable> Class<T> getPushableClassFromString(String className) throws ClassNotFoundException {
		ClassLoader classLoader = applicationContext.getClassLoader();
		log.info("PushableScheduledTask::getPushableClassFromString({})", className);

		// FIXME This is unchecked type conversion :/
		return (Class<T>) classLoader.loadClass(className);
	}

  @SuppressWarnings("unchecked")
  protected <T extends Pushable> PushableDatabaseService<Pushable> getPushableDatabaseServiceForPushableType( Class<T> pushableType ) {
    return (PushableDatabaseService<Pushable>) beanContext.getBean( Argument.of(PushableDatabaseService.class, pushableType) ); // Use argument specify core type plus any generic...
  }


  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Pushable> findById( Class<? extends Pushable> type, UUID id ) {
    return getPushableDatabaseServiceForPushableType(type).findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( Class<? extends Pushable> type, UUID id ) {
    return getPushableDatabaseServiceForPushableType(type).existsById(id);
  }

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<Long> deleteById( Class<? extends Pushable> type, UUID id ) {
		// We need to deregister any DutyCycleTasks associated with this source
		return Flux.from(reactiveDutyCycleTaskRepository.findAllByReference(id.toString()))
			.flatMap(dct -> Mono.from(reactiveDutyCycleTaskRunner.removeTask(dct))) // Remove the tasks if they exist
			.switchIfEmpty(Flux.just(0L)) // If no task to remove, just return 0L so we can still remove Source
			.then(Mono.from(getPushableDatabaseServiceForPushableType(type).deleteById(id)));
	}

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> getFeed(Class<? extends Pushable> type ) {
    return getPushableDatabaseServiceForPushableType(type).getFeed();
  }

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> ensurePushable( Pushable psh ) {
    return Mono.from(getPushableDatabaseServiceForPushableType(psh.getClass()).ensurePushable(psh))
      .flatMap(persistedPushable -> registerPushableTask(psh.getClass(), psh)
				.flatMap(dct -> Mono.just(persistedPushable)));
  }

  @NonNull
  @Transactional
  public Publisher<? extends Pushable> update( Pushable psh ) {
    return getPushableDatabaseServiceForPushableType(psh.getClass()).update(psh);
  }

  // What to do with a completed pushTask run
  @NonNull
  @Transactional
  public Publisher<Boolean> complete( Pushable psh ) {
    return getPushableDatabaseServiceForPushableType(psh.getClass()).complete(psh);
  }

  @NonNull
  @Transactional
  public Publisher<Source> getSource(Pushable psh) {
    return Mono.from(sourceService.findById(psh.getSourceType(), psh.getSourceId()));
  }

  @NonNull
  @Transactional
  public Publisher<Destination> getDestination(Pushable psh) {
    return Mono.from(destinationService.findById(psh.getDestinationType(), psh.getDestinationId()));
  }

  @NonNull
  @Transactional
  public Publisher<Tuple2<Source, Destination>> getSourceAndDestination(Pushable psh) {
    return Mono.from(getSource(psh))
      .flatMap(src -> Mono.from(getDestination(psh)).flatMap(dest -> Mono.just(Tuples.of(src, dest))));
  }
}
