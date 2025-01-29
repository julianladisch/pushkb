package com.k_int.pushKb.interactions.folio.api;

import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.interactions.folio.storage.FolioTenantRepository;

// FIXME This should be Auth protected
@Controller("/destinations/foliodestination/tenant")
@Slf4j
public class FolioTenantController extends CrudControllerImpl<FolioTenant> {
  public FolioTenantController(FolioTenantRepository repository) {
    super(repository);
  }
}
