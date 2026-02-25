package com.k_int.pushKb.crud;

import java.util.UUID;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.MediaType;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.VndError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class CrudControllerImpl<T extends HasId> implements CrudController<T> {
	private final CrudDatabaseService<T> service;

  public CrudControllerImpl(CrudDatabaseService<T> service) {
		this.service = service;
  }

	@Override
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	public Mono<T> post(@Valid @Body T t) {
		UUID generatedId = service.generateUUIDFromObject(t);
		t.setId(generatedId);

		return Mono.from(service.existsById(generatedId))
			.flatMap(exists -> {
				if (exists) {
					// Return a clean Conflict instead of a 500 or a fake 200
					return Mono.error(new HttpStatusException(
						HttpStatus.CONFLICT,
						"Resource with ID " + generatedId + " already exists. Use PUT to update."
					));
				}
				return Mono.from(service.save(t));
			});
	}

	@SingleResult // A Page is a single result
  @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
  public Mono<Page<T>> list(@Valid Pageable pageable) {
    return Mono.from(service.findAll(pageable));
  }

  @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<T> get(
    @Parameter UUID id
  ) {
    return Mono.from(service.findById(id));
  }

  @Put(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<T> put(
    @Parameter UUID id,
    @Valid @Body T t
  ) {
		// Set the id on the object FIRST so we definitely are managing a resource with id matching the PUT
		t.setId(id);
		return Mono.from(service.findById(id))
			.flatMap(resource -> {
				// FIXME this seems a bit silly but we have to ensure that we're updating the CORRECT id, not what's in the body
				UUID idCheck = service.generateUUIDFromObject(t);
				if (!id.equals(idCheck)) {
					throw new HttpStatusException(
						io.micronaut.http.HttpStatus.BAD_REQUEST,
						"This update operation would result in a change of the deterministic ID. Immutable fields cannot be modified."
					);
				}

				return Mono.just(resource);
			})
			.flatMap(bool -> Mono.from(service.update(t)));
  }

  // I'm not sure about having this return just a Long
  @Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	@Status(HttpStatus.NO_CONTENT)
	public Mono<Void> delete(
    @Parameter UUID id
  ) {
		return Mono.from(service.findById(id))
			.switchIfEmpty(Mono.error(new HttpStatusException(
				HttpStatus.NOT_FOUND,
				"Resource not found with ID: " + id
			)))
			.flatMap(resource -> Mono.from(service.delete(resource)))
			.then();
  }

  @Get(uri = "/count", produces = MediaType.APPLICATION_JSON)
	public Mono<Long> count() {
  return Mono.from(service.count());
  }
}
