package com.k_int.pushKb.transform.services;

import com.k_int.pushKb.transform.model.Transform;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import org.reactivestreams.Publisher;

import java.util.UUID;

public interface TransformDatabaseService<T extends Transform> {
  @NonNull
  @SingleResult
  @Transactional
  Publisher<T> findById( UUID id );

  @NonNull
  @SingleResult
  @Transactional
  Publisher<Boolean> existsById( UUID id );

  @NonNull
  @Transactional
  Publisher<T> list();

  @NonNull
  @SingleResult
  @Transactional
  Publisher<T> save( T t );

  @NonNull
  @SingleResult
  @Transactional
  Publisher<T> update( T t );

	@NonNull
	@SingleResult
	@Transactional
	Publisher<T> saveOrUpdate( T t );
}
