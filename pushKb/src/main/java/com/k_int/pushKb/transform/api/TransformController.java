package com.k_int.pushKb.transform.api;

import com.k_int.pushKb.transform.services.TransformService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller("/transforms")
@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
public class TransformController implements TransformApi{
	@Get(uri = "/implementers", produces = MediaType.APPLICATION_JSON)
	public Mono<Page<String>> getImplementers(@Valid Pageable pageable) {

		return Flux.fromIterable(TransformService.transformImplementers)
			.map(Class::toString)
			.collectList()// Get the full list... this should only ever be relatively small anyway
			.map(implementorList -> {

				int start = (int) pageable.getOffset();
				int end = Math.min(start + pageable.getSize(), implementorList.size());

				List<String> subList = (start >= implementorList.size())
					? List.of()
					: implementorList.subList(start, end);

				return Page.of(subList, pageable, (long) implementorList.size());
			});
	}
}
