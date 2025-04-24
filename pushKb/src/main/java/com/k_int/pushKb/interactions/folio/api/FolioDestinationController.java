package com.k_int.pushKb.interactions.folio.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.storage.FolioDestinationRepository;

@Controller("/destinations/foliodestination")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class FolioDestinationController extends CrudControllerImpl<FolioDestination> {
  public FolioDestinationController(FolioDestinationRepository repository) {
    super(repository);
  }
}
