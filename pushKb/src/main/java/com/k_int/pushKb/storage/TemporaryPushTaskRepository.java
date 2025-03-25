package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface TemporaryPushTaskRepository extends ReactiveStreamsPageableRepository<TemporaryPushTask, UUID> {
  // Unique up to pushTask/filterContext
  @NonNull
  @SingleResult
  Publisher<Boolean> existsById(@Nullable UUID id);
  
  // Unique up to pushTask/filterContext
  @NonNull
  @SingleResult
  @Join("pushTask")
  Publisher<TemporaryPushTask> findById(@Nullable UUID id);

  @NonNull
  @Join("pushTask")
	Publisher<TemporaryPushTask> findByPushTask(PushTask pushTask);

  @NonNull
  @Join("pushTask")
	Publisher<TemporaryPushTask> findByPushTaskAndFilterContext(PushTask pushTask, String filterContext);

  @NonNull
  @Join("pushTask")
	Publisher<TemporaryPushTask> listOrderByPushTaskAndFilterContext();
}
