package com.k_int.pushKb.crud;

import java.util.List;
import java.util.UUID;

import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public abstract class CrudControllerImpl<T extends HasId> implements CrudController<T> {
	private final CrudDatabaseService<T> service;

  public CrudControllerImpl(CrudDatabaseService<T> service) {
		this.service = service;
  }

  @Override
  @Post(uri = "/", produces = MediaType.APPLICATION_JSON)
  public Publisher<T> post(
    @Valid @Body T t
  ) {
		t.setId(service.generateUUIDFromObject(t));
		return service.save(t);
  }

  @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
  public Publisher<List<T>> list(@Valid Pageable pageable) {

    return Flux.from(service.findAll(pageable)).map(Page::getContent);
  }

  @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
  public Publisher<T> get(
    @Parameter UUID id
  ) {
    return service.findById(id);
  }

  @Put(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
  public Publisher<T> put(
    @Parameter UUID id,
    @Valid @Body T t
  ) {
    // FIXME this seems a bit silly but we have to ensure that we're updating the CORRECT id, not what's in the body
    t.setId(id);
    UUID idCheck = service.generateUUIDFromObject(t);
  
    // FIXME we probably need a better way to do this more generically
    if (!id.equals(idCheck)) {
      throw new RuntimeException("This save operation would result in a change of id");
    }

    // TODO perhaps we ought to do an existsByID here?

    return service.update(t);
  }

  // FIXME I'm not sure about having this return just a Long
  @Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
  public Publisher<Long> delete(
    @Parameter UUID id
  ) {
  return service.deleteById(id);
  }

  @Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
  public Publisher<Long> count() {
  return service.count();
  }
}
