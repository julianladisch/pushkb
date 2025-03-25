package com.k_int.pushKb.transform.services;

import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.model.Transform;
import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
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
	protected <T extends Transform> TransformDatabaseService<Transform> getTransformDatabaseServiceForTransformType( Class<T> type ) {
		return (TransformDatabaseService<Transform>) beanContext.getBean( Argument.of(TransformDatabaseService.class, type) ); // Use argument specify core type plus any generic...
	}

	public  Publisher<? extends Transform> findById( Class<? extends Transform> type, UUID id ) {
		return getTransformDatabaseServiceForTransformType(type).findById(id);
	}

	public Publisher<Boolean> existsById( Class<? extends Transform> type, UUID id ) {
		return getTransformDatabaseServiceForTransformType(type).existsById(id);
	}

	public Publisher<? extends Transform> list(Class<? extends Transform> type) {
		return getTransformDatabaseServiceForTransformType(type).list();
	}

	public Publisher<? extends Transform> save( Class<? extends Transform> type, Transform t ) {
		return getTransformDatabaseServiceForTransformType(type).save(t);
	}

	public Publisher<? extends Transform> update( Class<? extends Transform> type, Transform t ) {
		return getTransformDatabaseServiceForTransformType(type).update(t);
	}

	public Publisher<? extends Transform> saveOrUpdate( Class<? extends Transform> type, Transform t ) {
		return getTransformDatabaseServiceForTransformType(type).saveOrUpdate(t);
	}
}
