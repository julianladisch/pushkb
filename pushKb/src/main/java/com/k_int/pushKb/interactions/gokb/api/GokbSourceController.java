package com.k_int.pushKb.interactions.gokb.api;

import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.storage.GokbSourceRepository;

// FIXME This should be Auth protected
@Controller("/sources/gokbsource")
@Slf4j
public class GokbSourceController extends CrudControllerImpl<GokbSource> {
  public GokbSourceController(GokbSourceRepository repository) {
    super(repository);
  }

  // TODO would probably be nice to get /sources/gokbsource/<id>/recordCount to work.
  // We only have one source right now, but that could be an intermediate GenericSourceController
  // For now you can access with /sourcerecords/count?sourceId=<id>

    // TODO we may need an API way to clear the sourceRecord queue for a given source...
    // as above maybe GenericSourceController or something
}
