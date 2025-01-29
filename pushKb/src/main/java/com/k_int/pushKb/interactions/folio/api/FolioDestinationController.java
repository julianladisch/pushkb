package com.k_int.pushKb.interactions.folio.api;

import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.storage.FolioDestinationRepository;
// FIXME This should be Auth protected
@Controller("/destinations/foliodestination")
@Slf4j
public class FolioDestinationController extends CrudControllerImpl<FolioDestination> {
  public FolioDestinationController(FolioDestinationRepository repository) {
    super(repository);
  }
}
