package com.k_int.pushKb.interactions.gokb.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;

import com.k_int.pushKb.crud.CrudControllerImpl;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.storage.GokbSourceRepository;

@Controller("/sources/gokbsource")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
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
