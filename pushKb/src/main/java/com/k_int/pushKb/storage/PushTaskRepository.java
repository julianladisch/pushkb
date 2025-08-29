package com.k_int.pushKb.storage;

import com.k_int.pushKb.crud.ReactiveStreamsPageableRepositoryUUID5;
import com.k_int.pushKb.model.PushTask;

import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface PushTaskRepository extends ReactiveStreamsPageableRepositoryUUID5<PushTask> {
  @NonNull
	Publisher<PushTask> findBySourceIdAndDestinationId(UUID sourceId, UUID destinationID);

  @NonNull
	Publisher<PushTask> listOrderBySourceIdAndDestinationIdAndId();

  @Override
  default UUID generateUUIDFromObject(PushTask obj) {
    return PushTask.generateUUIDFromPushTask(obj);
  }
}
