package com.k_int.pushKb.interactions.folio.api;

import com.k_int.pushKb.interactions.folio.services.FolioTenantDatabaseService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/destinations/foliodestination/tenant")
@Slf4j
public class FolioTenantController extends CrudControllerImpl<FolioTenant> {
  public FolioTenantController(FolioTenantDatabaseService databaseService) {
    super(databaseService);
  }
}
