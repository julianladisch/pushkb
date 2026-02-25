package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.services.FolioDestinationDatabaseService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller("/destinations/foliodestination")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class FolioDestinationController extends CrudControllerImpl<FolioDestination> implements FolioDestinationApi {
	FolioDestinationDatabaseService databaseService;
  public FolioDestinationController(FolioDestinationDatabaseService databaseService) {
    super(databaseService);
		this.databaseService = databaseService;
  }


	@Override
	@Post(uri = "/", produces = MediaType.APPLICATION_JSON)
	public Mono<FolioDestination> post(@Valid @Body FolioDestination dest) {
		UUID generatedId = databaseService.generateUUIDFromObject(dest);
		dest.setId(generatedId);

		return Mono.from(databaseService.existsById(generatedId))
			.flatMap(exists -> {
				if (exists) {
					return Mono.error(new HttpStatusException(
						HttpStatus.CONFLICT,
						String.format("FolioDestination '%s' already exists.", generatedId)
					));
				}

				// We need to use ensureDestination here to make sure that side effects happen as expected
				// Such as Vault handling etc.
				return Mono.from(databaseService.ensureDestination(dest));
			});
	}
}
