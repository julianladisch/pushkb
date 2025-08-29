package com.k_int.pushKb.interactions.gokb.api;

import com.k_int.pushKb.interactions.gokb.services.GokbSourceDatabaseService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import org.reactivestreams.Publisher;

@Controller("/sources/gokbsource")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class GokbSourceController extends CrudControllerImpl<GokbSource> {
	GokbSourceDatabaseService databaseService;
  public GokbSourceController(GokbSourceDatabaseService databaseService) {
    super(databaseService);
		this.databaseService = databaseService;
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
		return databaseService.ensureSource(src);
	}
}
