package com.k_int.pushKb.crud;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import org.reactivestreams.Publisher;

import java.util.UUID;

public interface CrudDatabaseService<T> {
	ReactiveStreamsPageableRepositoryUUID5<T> getRepository();

	default UUID generateUUIDFromObject(T obj) {
		return getRepository().generateUUIDFromObject(obj);
	}

	@Transactional
	default Publisher<Page<T>> findAll(@NonNull Pageable pageable) {
		return getRepository().findAll(pageable);
	}

	@NonNull
	@Transactional
	default Publisher<T> findAll(@NonNull Sort sort) {
		return getRepository().findAll(sort);
	}

	@NonNull
	@SingleResult
	@Transactional
	default Publisher<T> save(@NonNull T entity) {
		return getRepository().save(entity);
	}

	@NonNull
	@Transactional
	default Publisher<T> saveAll(@NonNull Iterable<T> entities) {
		return getRepository().saveAll(entities);
	}

	@NonNull
	@Transactional
	default Publisher<T> update(@NonNull T entity) {
		return getRepository().update(entity);
	}

	@NonNull
	@Transactional
	default Publisher<T> updateAll(@NonNull Iterable<T> entities) {
		return getRepository().updateAll(entities);
	}


	@NonNull
	@SingleResult
	@Transactional
	default Publisher<T> findById(@NonNull UUID id) {
		return getRepository().findById(id);
	}


	@SingleResult
	@NonNull
	@Transactional
	default Publisher<Boolean> existsById(@NonNull UUID id) {
		return getRepository().existsById(id);
	}

	@NonNull
	@Transactional
	default Publisher<T> findAll() {
		return getRepository().findAll();
	}

	@SingleResult
	@NonNull
	@Transactional
	default Publisher<Long> count() {
		return getRepository().count();
	}

	// Let's try to avoid having to do side effects in two places...
//	@NonNull
//	@SingleResult
//	@Transactional
//	default Publisher<Long> deleteById(@NonNull UUID id) {
//		return getRepository().deleteById(id);
//	}

	@SingleResult
	@NonNull
	@Transactional
	default Publisher<Long> delete(@NonNull T entity) {
		return getRepository().delete(entity);
	}

	@SingleResult
	@NonNull
	@Transactional
	default Publisher<Long> deleteAll(@NonNull Iterable<? extends T> entities) {
		return getRepository().deleteAll(entities);
	}

	@NonNull
	@Transactional
	default Publisher<Long> deleteAll() {
		return getRepository().deleteAll();
	}
}
