package com.k_int.pushKb.interactions.gokb.api;

import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;


import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.storage.GokbRepository;


// FIXME This should be Auth protected
@Controller("/sources/gokbsource/gokb")
@Slf4j
public class GokbController extends CrudControllerImpl<Gokb> {
  public GokbController(GokbRepository gokbRepository) {
    super(gokbRepository);
  }
}
