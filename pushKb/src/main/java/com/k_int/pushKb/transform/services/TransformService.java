package com.k_int.pushKb.transform.services;

import com.k_int.pushKb.transform.TransformType;
import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.model.Transform;
import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Set;
import java.util.UUID;

@Singleton
public class TransformService {
	private final BeanContext beanContext;

	public static final Set<Class<? extends Transform>> transformImplementors = Set.of(ProteusTransform.class);

	public TransformService (
		BeanContext beanContext
	) {
		this.beanContext = beanContext;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Transform> TransformImplementationService<Transform> getTransformImplementationServiceForTransformType(Class<T> type) {
		return (TransformImplementationService<Transform>) beanContext.getBean( Argument.of(TransformImplementationService.class, type) ); // Use argument specify core type plus any generic...
	}

	// Specifically handle getting JSON_TO_JSON type transforms
	@SuppressWarnings("unchecked")
	protected <T extends Transform> JsonToJsonTransformImplementationService<Transform> getJsonToJsonTransformImplementationServiceForTransformType(Class<T> type) {
		return (JsonToJsonTransformImplementationService<Transform>) beanContext.getBean( Argument.of(JsonToJsonTransformImplementationService.class, type) ); // Use argument specify core type plus any generic...
	}

	public  Publisher<? extends Transform> findById( Class<? extends Transform> type, UUID id ) {
		return getTransformImplementationServiceForTransformType(type).findById(id);
	}

	public Publisher<Boolean> existsById( Class<? extends Transform> type, UUID id ) {
		return getTransformImplementationServiceForTransformType(type).existsById(id);
	}

	public Publisher<? extends Transform> list(Class<? extends Transform> type) {
		return getTransformImplementationServiceForTransformType(type).list();
	}

	public Publisher<? extends Transform> save( Class<? extends Transform> type, Transform t ) {
		return getTransformImplementationServiceForTransformType(type).save(t);
	}

	public Publisher<? extends Transform> update( Class<? extends Transform> type, Transform t ) {
		return getTransformImplementationServiceForTransformType(type).update(t);
	}

	public Publisher<? extends Transform> saveOrUpdate( Class<? extends Transform> type, Transform t ) {
		return getTransformImplementationServiceForTransformType(type).saveOrUpdate(t);
	}

	//  Add an implementation transform for each transform type... this seems really clunky
	public JsonNode transformJsonToJson(Class<? extends Transform> type, Transform t, JsonNode input) throws Exception {
		if (t.getType() == TransformType.JSON_TO_JSON) {
			return getJsonToJsonTransformImplementationServiceForTransformType(type).transform(t, input);
		}

		throw new RuntimeException("Cannot perform transformJsonToJson on transform not of type JSON_TO_JSON");
	}
}
