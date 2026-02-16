package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.services.GokbSourceDatabaseService;
import com.k_int.pushKb.services.SourceService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.annotation.*;
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
  public GokbSourceController(
		GokbSourceDatabaseService databaseService,
		SourceService sourceService
	) {
    super(databaseService);
		this.sourceService = sourceService;
  }

  // TODO would probably be nice to get /sources/gokbsource/<id>/recordCount to work.
  // We only have one source right now, but that could be an intermediate GenericSourceController
  // For now you can access with /sourcerecords/count?sourceId=<id>

    // TODO we may need an API way to clear the sourceRecord queue for a given source...
    // as above maybe GenericSourceController or something

	// We need to use ensureSource here to make sure that side effects happen as expected
	// Such as DutyCycleTask creation etc.
	@Override
	public Mono<GokbSource> post(
		@Valid @Body GokbSource src
	) {
		return Mono.from(sourceService.ensureSource(src)).map(GokbSource::castFromSource);
	}

	// We need to use sourceService here to make sure that side effects happen as expected
	// Such as DutyCycleTask removal etc.
	@Override
	public Mono<Long> delete(
		@Parameter UUID id
	) {
		return Mono.from(sourceService.findById(GokbSource.class, id))
			.switchIfEmpty(Mono.error(new IllegalStateException("PushTask not found with ID: " + id)))
			.flatMap(gkbs -> Mono.from(sourceService.delete(GokbSource.class, (GokbSource) gkbs)));
	}

	// Reset pointer endpoint
	@SingleResult
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
