package com.k_int.pushKb.converters;

import com.k_int.pushKb.model.Destination;

import io.micronaut.context.BeanContext;
import jakarta.inject.Singleton;

@Singleton
public class DestinationClassAttributeConverter extends ClassAttributeConverter<Destination> {
	public DestinationClassAttributeConverter ( BeanContext beanContext ) {
    super(beanContext);
  }
}