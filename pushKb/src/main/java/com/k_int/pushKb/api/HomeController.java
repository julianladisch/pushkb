package com.k_int.pushKb.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
//import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {
  // TODO figure out what's happening here
  @Secured(SecurityRule.IS_ANONYMOUS)
  //@View("home")
  @Get 
  public Map<String, Object> index() {
      return new HashMap<>();
  }
}
