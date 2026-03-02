package com.k_int.pushKb.transform.api;

import com.k_int.pushKb.transform.model.ProteusTransform;
import com.k_int.pushKb.transform.services.ProteusTransformImplementationService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller("/transforms/proteustransform")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ProteusTransformController extends CrudControllerImpl<ProteusTransform> implements ProteusTransformApi {
	ProteusTransformImplementationService implementationService;

	public ProteusTransformController(ProteusTransformImplementationService implementationService) {
		super(implementationService);
		this.implementationService = implementationService;
	}

	@Hidden // This isn't supported so hide it from the docs. Hidden cannot be on the interface as this overrides
	@Override
	@Post(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<ProteusTransform> post(
		@Valid @Body ProteusTransform pt
	) {
		return Mono.error(new HttpStatusException(
			HttpStatus.METHOD_NOT_ALLOWED,
			"POST is not currently supported on ProteusTransform - only GET is supported."
		));
	}

	@Hidden // This isn't supported so hide it from the docs. Hidden cannot be on the interface as this overrides
	@Override
	@Put(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<ProteusTransform> put(
		@Parameter UUID id,
		@Valid @Body ProteusTransform pt
	) {
		return Mono.error(new HttpStatusException(
			HttpStatus.METHOD_NOT_ALLOWED,
			"PUT is not currently supported on ProteusTransform - only GET is supported."
		));
	}

	@Hidden // This isn't supported so hide it from the docs. Hidden cannot be on the interface as this overrides
	@Override
	@Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<Void> delete(
		@Parameter UUID id
	) {
		return Mono.error(new HttpStatusException(
			HttpStatus.METHOD_NOT_ALLOWED,
			"DELETE is not currently supported on ProteusTransform - only GET is supported."
		));
	}
}
