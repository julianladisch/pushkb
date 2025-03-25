package com.k_int.pushKb.transform.services;

import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.storage.ProteusTransformRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Singleton
public class ProteusTransformDatabaseService implements TransformDatabaseService<ProteusTransform> {
	private final ProteusTransformRepository proteusTransformRepository;

	public ProteusTransformDatabaseService(
		ProteusTransformRepository proteusTransformRepository
	) {
		this.proteusTransformRepository = proteusTransformRepository;
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<ProteusTransform> findById(UUID id ) {
		return proteusTransformRepository.findById(id);
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<Boolean> existsById( UUID id ) {
		return proteusTransformRepository.existsById(id);
	}

	@NonNull
	@Transactional
	public Publisher<ProteusTransform> list() {
		return proteusTransformRepository.list();
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<ProteusTransform> save( ProteusTransform t ) {
		return proteusTransformRepository.save(t);
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<ProteusTransform> update( ProteusTransform t ) {
		return proteusTransformRepository.update(t);
	}

	@NonNull
	@SingleResult
	@Transactional
	public Publisher<ProteusTransform> saveOrUpdate( ProteusTransform t ) {
		return proteusTransformRepository.saveOrUpdate(t);
	}
}
