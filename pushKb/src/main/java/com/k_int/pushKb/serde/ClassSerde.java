package com.k_int.pushKb.serde;

import java.io.IOException;
import java.util.Objects;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serde;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ClassSerde implements Serde<Class<?>> {
  @Override
  public Class<?> deserialize(
    Decoder decoder,
    DecoderContext context,
    Argument<? super Class<?>> type
  ) throws IOException {
    try {
      String clazzName = decoder.decodeString();
      Class<?> clazz = Class.forName(clazzName);
      return clazz;
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void serialize(
    Encoder encoder,
    EncoderContext context,
    Argument<? extends Class<?>> type, Class<?> clazz
  ) throws IOException {
    Objects.requireNonNull(clazz, "Class cannot be null");
    encoder.encodeString(clazz.getName());
  }
}
