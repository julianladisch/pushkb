package com.k_int.pushKb.services;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.k_int.taskscheduler.storage.ReactiveDutyCycleTaskRepository;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.Source;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@Slf4j
public class SourceService {
  private final BeanContext beanContext;
  private final ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner;
	private final ReactiveDutyCycleTaskRepository reactiveDutyCycleTaskRepository; // Should this be handled by the runner?

  public static final Set<Class<? extends Source>> sourceImplementers = Set.of(GokbSource.class);

  public SourceService (
    BeanContext beanContext,
    ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner,
		ReactiveDutyCycleTaskRepository reactiveDutyCycleTaskRepository
    ) {
    this.beanContext = beanContext;
    this.reactiveDutyCycleTaskRunner = reactiveDutyCycleTaskRunner;
		this.reactiveDutyCycleTaskRepository = reactiveDutyCycleTaskRepository;
  }

  // Fast way to register ingest tasks directly
  public Mono<DutyCycleTask> registerIngestTask(Class<? extends Source> type, Source s) {
    String srcId = s.getId().toString();
    return reactiveDutyCycleTaskRunner.registerTask(
			(long) (1000 * 60 * 60),
			srcId,
      "IngestScheduledTask",
      Map.of(
        "source", srcId,
        "source_class", type.getName()
      )
    );
  }

  @SuppressWarnings("unchecked")
  protected <T extends Source> SourceDatabaseService<Source> getSourceDatabaseServiceForSourceType( Class<T> sourceType ) {
    return (SourceDatabaseService<Source>) beanContext.getBean( Argument.of(SourceDatabaseService.class, sourceType) ); // Use argument specify core type plus any generic...
  }

  // Replaced with service calls, one more layer of abstraction
/*   @SuppressWarnings("unchecked")
  protected <T extends Source> SourceRepository<Source> getRepositoryForSourceType( Class<T> sourceType ) {
    return (SourceRepository<Source>) beanContext.getBean( Argument.of(SourceRepository.class, sourceType) ); // Use argument specify core type plus any generic...
  } */


  @SuppressWarnings("unchecked")
  protected <T extends Source> SourceFeedService<Source> getFeedServiceForSourceType( Class<T> sourceType ) {
    return (SourceFeedService<Source>) beanContext.getBean( Argument.of(SourceFeedService.class, sourceType) ); // Use argument specify core type plus any generic...
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> findById( Class<? extends Source> type, UUID id ) {
    return getSourceDatabaseServiceForSourceType(type).findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> castToSource( Class<? extends Source> type, JsonNode sourceObject ) {
    return getSourceDatabaseServiceForSourceType(type).castToSource(sourceObject);
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<Boolean> existsById( Class<? extends Source> type, UUID id ) {
    return getSourceDatabaseServiceForSourceType(type).existsById(id);
  }

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<Long> deleteById( Class<? extends Source> type, UUID id ) {
		// We need to deregister any DutyCycleTasks associated with this source
		return Flux.from(reactiveDutyCycleTaskRepository.findAllByReference(id.toString()))
			.flatMap(dct -> Mono.from(reactiveDutyCycleTaskRunner.removeTask(dct))) // Remove the tasks if they exist
			.switchIfEmpty(Flux.just(0L)) // If no task to remove, just return 0L so we can still remove Source
			.then(Mono.from(getSourceDatabaseServiceForSourceType(type).deleteById(id)));
	}

  @NonNull
  @Transactional
  public Publisher<? extends Source> list(Class<? extends Source> type ) {
    return getSourceDatabaseServiceForSourceType(type).list();
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> ensureSource( Source src ) {
    log.debug("SourceService::ensureSource called with({})", src);
    return Mono.from(getSourceDatabaseServiceForSourceType(src.getClass()).ensureSource(src))
    .flatMap(persistedSource -> {
      return registerIngestTask(src.getClass(), src)
        .flatMap(dct -> Mono.just(persistedSource))
        .switchIfEmpty(Mono.just(persistedSource)); // Make sure even if task was already registered we still get back the source
    });
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> save( Source src ) {
    log.debug("SourceService::save called with({})", src);
    return Mono.from(getSourceDatabaseServiceForSourceType(src.getClass()).save(src));
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> update( Source src ) {
    log.debug("SourceService::update called with({})", src);
    return Mono.from(getSourceDatabaseServiceForSourceType(src.getClass()).update(src));
  }

  @NonNull
  @SingleResult
  @Transactional
  public Publisher<? extends Source> saveOrUpdate( Source src ) {
    log.debug("SourceService::saveOrUpdate called with({})", src);
    return Mono.from(getSourceDatabaseServiceForSourceType(src.getClass()).saveOrUpdate(src));
  }

  // Expects to return the source AFTER feed completes with all fields saved and up to date
  public Publisher<Source> triggerIngestForSource(Source source) {
    SourceFeedService<Source> sourceFeedService = getFeedServiceForSourceType(source.getClass());
    SourceDatabaseService<Source> sourceDatabaseService = getSourceDatabaseServiceForSourceType(source.getClass());

    // Set lastIngestStarted
    return Mono.from(sourceDatabaseService.setLastIngestStarted(source))
      .flatMap(sourceFeedService::fetchSourceRecords).flatMap(src -> {
        // Set lastIngestCompleted
        return Mono.from(sourceDatabaseService.setLastIngestCompleted(src));
      });
  }
}
