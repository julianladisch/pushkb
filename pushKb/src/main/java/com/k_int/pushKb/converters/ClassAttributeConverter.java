package com.k_int.pushKb.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.context.BeanContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

@Singleton
public class ClassAttributeConverter<T> implements AttributeConverter<Class<? extends T>, String> {
	private final BeanContext beanContext;
	// Logging
	static final Logger log = LoggerFactory.getLogger(SourceClassAttributeConverter.class);

	public ClassAttributeConverter ( BeanContext beanContext ) {
    this.beanContext = beanContext;
  }

	@Override
	public String convertToPersistedValue(Class<? extends T> clazz, ConversionContext context) {
		return clazz.getName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends T> convertToEntityValue(String className, ConversionContext context) {
		try {
			return (Class<? extends T>) beanContext.getClassLoader().loadClass(className);
		} catch (Exception e) {
			return null;
		}
	}
}
