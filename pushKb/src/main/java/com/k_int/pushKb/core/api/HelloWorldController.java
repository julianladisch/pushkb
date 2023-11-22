package com.k_int.pushKb.core.api;

import com.k_int.pushKb.storage.HelloWorldRepository;

import io.micronaut.http.annotation.Controller;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;

import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/helloworld")
@Tag(name = "Hello World API")
public class HelloWorldController {
	private final HelloWorldRepository helloWorldRepository;

	public HelloWorldController(
		HelloWorldRepository helloWorldRepository
  ) {
		this.helloWorldRepository = helloWorldRepository;
	}
}

