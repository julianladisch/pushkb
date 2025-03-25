package com.k_int.pushKb.transform.storage;

import com.k_int.pushKb.transform.model.ProteusTransform;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import org.reactivestreams.Publisher;

import java.util.UUID;

public interface ProteusTransformRepository extends TransformRepository<ProteusTransform> {
	@NonNull
	@SingleResult
	Publisher<Boolean> existsById(@Nullable UUID id);
	Publisher<ProteusTransform> findById(@Nullable UUID id);

	@NonNull
	Publisher<ProteusTransform> list();
}
