package com.k_int.pushKb.services;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
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

@Singleton
@Slf4j
public class SourceService {
  private final BeanContext beanContext;
  private final ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner;

  public SourceService (
    BeanContext beanContext,
    ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner
    ) {
    this.beanContext = beanContext;
    this.reactiveDutyCycleTaskRunner = reactiveDutyCycleTaskRunner;
  }

  // Fast way to register ingest tasks directly
  public Mono<DutyCycleTask> registerIngestTask(Class<? extends Source> type, Source s) {
    String srcId = s.getId().toString();
    return reactiveDutyCycleTaskRunner.registerTask(
      Long.valueOf(1000*60*60),
      srcId.toString(),
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
  public Publisher<Boolean> existsById( Class<? extends Source> type, UUID id ) {
    return getSourceDatabaseServiceForSourceType(type).existsById(id);
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
    return Mono.from(getSourceDatabaseServiceForSourceType(src.getClass()).ensureSource(src))
    .flatMap(persistedSource -> {
      return registerIngestTask(src.getClass(), src)
        .flatMap(dct -> Mono.just(persistedSource));
    });
  }

  public Publisher<Class<? extends Source>> getSourceImplementors() {
    HashSet<Class<? extends Source>> sourceClasses = new HashSet<Class<? extends Source>>();
    // TODO For now manually return set of all Source implementing classes (??)
    sourceClasses.add(GokbSource.class);

    return Flux.fromIterable(sourceClasses);
  }

  // Expects to return the source AFTER feed completes with all fields saved and up to date
  public Publisher<Source> triggerIngestForSource(Source source) {
    SourceFeedService<Source> sourceFeedService = getFeedServiceForSourceType(source.getClass());
    SourceDatabaseService<Source> sourceDatabaseService = getSourceDatabaseServiceForSourceType(source.getClass());

    // Set lastIngestStarted
    return Mono.from(sourceDatabaseService.setLastIngestStarted(source))
      .flatMap(src -> {
        return sourceFeedService.fetchSourceRecords(src);
      }).flatMap(src -> {
        // Set lastIngestCompleted
        return Mono.from(sourceDatabaseService.setLastIngestCompleted(src));
      });
  }
}
