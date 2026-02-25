package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.services.GokbSourceDatabaseService;
import com.k_int.pushKb.services.SourceService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller("/sources/gokbsource")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class GokbSourceController extends CrudControllerImpl<GokbSource> implements GokbSourceApi {
	SourceService sourceService;
	GokbSourceDatabaseService databaseService;
  public GokbSourceController(
		GokbSourceDatabaseService databaseService,
		SourceService sourceService
	) {
    super(databaseService);
		this.databaseService = databaseService;
		this.sourceService = sourceService;
  }

  // TODO would probably be nice to get /sources/gokbsource/<id>/recordCount to work.
  // We only have one source right now, but that could be an intermediate GenericSourceController
  // For now you can access with /sourcerecords/count?sourceId=<id>

    // TODO we may need an API way to clear the sourceRecord queue for a given source...
    // as above maybe GenericSourceController or something

	@Override
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	public Mono<GokbSource> post(@Valid @Body GokbSource src) {
		UUID generatedId = databaseService.generateUUIDFromObject(src);
		src.setId(generatedId);

		return Mono.from(databaseService.existsById(generatedId))
			.flatMap(exists -> {
				if (exists) {
					return Mono.error(new HttpStatusException(
						HttpStatus.CONFLICT,
						String.format("GokbSource '%s' already exists. Use PUT to update.", generatedId)
					));
				}

				// We need to use ensureSource here to make sure that side effects happen as expected
				// Such as DutyCycleTask creation etc.
				return Mono.from(sourceService.ensureSource(src)).map(GokbSource::castFromSource);
			});
	}

	// We need to use sourceService here to make sure that side effects happen as expected
	// Such as DutyCycleTask removal etc.
	@Override
	@Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Mono<Void> delete(@Parameter UUID id) {
		return Mono.from(sourceService.findById(GokbSource.class, id))
			.switchIfEmpty(Mono.error(
				new HttpStatusException(
					HttpStatus.NOT_FOUND,
					"GokbSource not found: " + id
				)
			))
			.flatMap(gkbs -> Mono.from(sourceService.delete(GokbSource.class, (GokbSource) gkbs)))
			.then();
	}

	// Reset pointer endpoint
	@SingleResult
	@Put(uri = "/{id}/resetPointer", produces = MediaType.APPLICATION_JSON)
	public Mono<GokbSource> resetPointer(
		@Parameter UUID id
	) {

		return Mono.from(sourceService.findById(GokbSource.class, id))
			.map(GokbSource::castFromSource)
			.flatMap(src -> {
				src.setPointer(null);

				return Mono.from(sourceService.update(src)).map(GokbSource::castFromSource);
			})
			.switchIfEmpty(Mono.error(new RuntimeException("No GokbSource found with id: " + id)));
	}
}
