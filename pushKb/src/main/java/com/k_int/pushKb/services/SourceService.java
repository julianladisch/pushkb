package com.k_int.pushKb.services;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
public class SourceService {
  private final BeanContext beanContext;
  public SourceService ( BeanContext beanContext ) {
    this.beanContext = beanContext;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Source> SourceRepository<T> getRepositoryForSourceType( Class<T> sourceType ) {
    return (SourceRepository<T>) beanContext.getBean( Argument.of(SourceRepository.class, sourceType) ); // Use argument specify core type plus any generic...
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Source> Publisher<T> findById( UUID id, Class<T> type ) {
    return getRepositoryForSourceType(type).findById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Source> Publisher<Boolean> existsById( UUID id, Class<T> type ) {
    return getRepositoryForSourceType(type).existsById(id);
  }

  @NonNull
  @SingleResult
  @Transactional
  public <T extends Source> Publisher<T> ensureSource( T src, Class<T> type ) {
    return getRepositoryForSourceType(type).ensureSource(src);
  }
}
