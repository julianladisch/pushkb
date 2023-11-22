package com.k_int.pushKb.core.api;

import java.util.UUID;

import com.k_int.pushKb.core.model.HelloWorld;
import com.k_int.pushKb.storage.HelloWorldRepository;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

//@Validated
// FIXME security needs implementing
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/hello")
@Tag(name = "Hello World API")
public class HelloWorldController {
	private final HelloWorldRepository helloWorldRepository;

	public HelloWorldController(
		HelloWorldRepository helloWorldRepository
  ) {
		this.helloWorldRepository = helloWorldRepository;
	}

  @Get("/{?pageable*}")
  public Mono<Page<HelloWorld>> list(@Parameter(hidden = true) @Valid Pageable pageable) {
    if (pageable == null) {
      pageable = Pageable.from(0, 100)
                         .order("name");
    }

    return Mono.from(helloWorldRepository.findAll(pageable));
  }

  @Get("/{id}")
  public Mono<HelloWorld> show(UUID id) {
          return Mono.from(helloWorldRepository.findById(id));
  }

  @Post("/")
  public Mono<HelloWorld> postHello(@Body HelloWorld helloworld) {

          if (helloworld.getId() == null) {
            return Mono.from(helloWorldRepository.save(helloworld));
          }

          return Mono.from(helloWorldRepository.existsById(helloworld.getId()))
                  .flatMap(exists -> Mono.fromDirect(exists ? helloWorldRepository.update(helloworld) : helloWorldRepository.save(helloworld)));
  }
}
