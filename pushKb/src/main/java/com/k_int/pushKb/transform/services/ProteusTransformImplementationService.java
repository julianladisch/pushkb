package com.k_int.pushKb.transform.services;

import com.k_int.proteus.ComponentSpec;
import com.k_int.pushKb.proteus.ProteusService;
import com.k_int.pushKb.transform.model.ProteusSpecSource;
import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.storage.ProteusTransformRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.UUID;

@Singleton
@Slf4j
public class ProteusTransformImplementationService implements JsonToJsonTransformImplementationService<ProteusTransform> {
	private final ProteusTransformRepository proteusTransformRepository;
	private final ProteusService proteusService;

	public ProteusTransformImplementationService(
		ProteusTransformRepository proteusTransformRepository,
		ProteusService proteusService
	) {
		this.proteusTransformRepository = proteusTransformRepository;
		this.proteusService = proteusService;
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

	public JsonNode transform(ProteusTransform t, JsonNode input) throws IOException {
		// Proteus implements JSON -> JSON
		ComponentSpec<JsonNode> proteusSpec;
		if (t.getSource() == ProteusSpecSource.FILE_SPEC) {
			proteusSpec = proteusService.loadSpec(t.getSpecFile());
		} else {
			proteusSpec = proteusService.loadSpec(t.getSpec());
		}

		return proteusService.convert(
			proteusSpec,
			input
		);
	}
}
