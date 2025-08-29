package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.services.FolioDestinationDatabaseService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Controller("/destinations/foliodestination")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class FolioDestinationController extends CrudControllerImpl<FolioDestination> {
	FolioDestinationDatabaseService databaseService;
  public FolioDestinationController(FolioDestinationDatabaseService databaseService) {
    super(databaseService);
		this.databaseService = databaseService;
  }

	// We need to use ensureSource here to make sure that side effects happen as expected
	// Such as DutyCycleTask creation etc.
	@Override
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	public Publisher<FolioDestination> post(
		@Valid @Body FolioDestination dest
	) {
		return Mono.from(databaseService.ensureDestination(dest));
	}
}
