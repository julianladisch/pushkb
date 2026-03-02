package com.k_int.pushKb.transform.services;

import com.k_int.pushKb.transform.model.Transform;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;

@Singleton
public interface JsonToJsonTransformImplementationService<T extends Transform> extends TransformDatabaseService<T> {
	JsonNode transform(T t, JsonNode input) throws Exception;
}
