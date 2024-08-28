package com.k_int.pushKb.services;

import java.util.UUID;
import java.util.HashSet;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceRecord;
//import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Singleton
@Slf4j
public class SourceService {
  private final BeanContext beanContext;
  public SourceService ( BeanContext beanContext ) {
    this.beanContext = beanContext;
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
    return getSourceDatabaseServiceForSourceType(src.getClass()).ensureSource(src);
  }

  public Publisher<Class<? extends Source>> getSourceImplementors() {
    HashSet<Class<? extends Source>> sourceClasses = new HashSet<Class<? extends Source>>();
    // For now manually return set of all Source implementing classes (??)
    sourceClasses.add(GokbSource.class);

    return Flux.fromIterable(sourceClasses);
  }

  public Publisher<SourceRecord> triggerIngestForSource(Source source) {
    SourceFeedService<Source> sourceFeedService = getFeedServiceForSourceType(source.getClass());
    return sourceFeedService.fetchSourceRecords(source);
  }
}
