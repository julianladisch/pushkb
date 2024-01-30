package com.k_int.pushKb.converters;

import com.k_int.pushKb.model.Source;

import io.micronaut.context.BeanContext;
import jakarta.inject.Singleton;

@Singleton
public class SourceClassAttributeConverter extends ClassAttributeConverter<Source> {
	public SourceClassAttributeConverter ( BeanContext beanContext ) {
    super(beanContext);
  }
}
