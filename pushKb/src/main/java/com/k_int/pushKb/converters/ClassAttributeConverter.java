package com.k_int.pushKb.converters;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

@Singleton
public class ClassAttributeConverter implements AttributeConverter<Class<?>, String> {

	@Override
	public String convertToPersistedValue(Class<?> clazz, ConversionContext context) {
		return clazz.getName();
	}

	@Override
	public Class<?> convertToEntityValue(String className, ConversionContext context) {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			context.reject(e);
			return null;
		}
	}
}
