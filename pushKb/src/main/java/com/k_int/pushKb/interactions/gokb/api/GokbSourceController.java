package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.services.GokbSourceDatabaseService;
import com.k_int.pushKb.services.SourceService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller("/sources/gokbsource")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class GokbSourceController extends CrudControllerImpl<GokbSource> {
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
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	public Publisher<GokbSource> post(
		@Valid @Body GokbSource src
	) {
		return Mono.from(sourceService.ensureSource(src)).map(GokbSource::castFromSource);
	}

	// We need to use sourceService here to make sure that side effects happen as expected
	// Such as DutyCycleTask removal etc.
	@Override
	@Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
	public Publisher<Long> delete(
		@Parameter UUID id
	) {
		return sourceService.deleteById(GokbSource.class, id);
	}

	// Reset pointer endpoint
	@Put(uri = "/{id}/resetPointer", produces = MediaType.APPLICATION_JSON)
	public Publisher<GokbSource> resetPointer(
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
