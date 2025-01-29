package com.k_int.pushKb.crud;

import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.Body;
import jakarta.validation.Valid;

public interface CrudController<T extends HasId> {
  public Publisher<T> post(@Valid @Body T obj);
  public Publisher<List<T>> list(@Valid Pageable pageable);
  public Publisher<T> get(@Parameter UUID id);
  public Publisher<T> put(@Parameter UUID id, @Valid @Body T obj);
  public Publisher<Long> delete(@Parameter UUID id);
}
