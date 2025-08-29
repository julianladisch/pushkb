package com.k_int.pushKb.services;

import com.k_int.pushKb.crud.CrudDatabaseService;
import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Pushable;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;

public interface PushableDatabaseService<T extends Pushable> extends CrudDatabaseService<T> {
  @NonNull
  @Transactional
	Publisher<T> ensurePushable(T psh);

  @NonNull
  @Transactional
	Publisher<T> getFeed();

  @NonNull
  @SingleResult
  @Transactional
	Publisher<Boolean> complete(T psh);
}
