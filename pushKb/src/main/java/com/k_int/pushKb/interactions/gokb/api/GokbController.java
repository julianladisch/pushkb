package com.k_int.pushKb.interactions.gokb.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.storage.GokbRepository;

@Controller("/sources/gokbsource/gokb")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class GokbController extends CrudControllerImpl<Gokb> {
  public GokbController(GokbRepository gokbRepository) {
    super(gokbRepository);
  }
}
