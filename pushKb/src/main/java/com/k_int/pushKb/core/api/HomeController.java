package com.k_int.pushKb.core.api;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

// FIXME Idk if I need this... getting 401 rn

//@Secured(SecurityRule.IS_AUTHENTICATED) 
@Controller  
public class HomeController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get  
    String index(Principal principal) {  
        return principal.getName();
    }
}