package com.k_int.pushKb;

import java.util.Collection;
import java.util.List;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.services.SourceService;
import com.k_int.pushKb.storage.SourceRepository;

import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Singleton
public class ApplicationStartupListener implements ApplicationEventListener<StartupEvent> {
	private final Environment environment;
  private final SourceService sourceService;

  private static final String BOOSTRAP_SOURCE_VAR = "BOOTSTRAP_SOURCES";

	public ApplicationStartupListener(
    Environment environment,
		SourceService sourceService
  ) {
		this.environment = environment;
		this.sourceService = sourceService;
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		log.info("Bootstrapping PushKB - onApplicationEvent");

    // FIXME this should use the BOOSTRAP_SOURCE_VAR, not always bootstrap, turned off for now for dev purposes
		/* if (environment.getProperty(BOOSTRAP_SOURCE_VAR, String.class)
				.orElse("false").equalsIgnoreCase("true")) { */
			log.info("Boostrapping sources");
      bootstrapSources();
		//}

		log.info("Exit onApplicationEvent");
	}

	private void bootstrapSources() {
		log.debug("bootstrapSources");
		Mono.just ( "start" )
			.flatMap( v -> Mono.from(ensureSource(
        "https://gokb.org/gokb/api",
        SourceCode.GOKB,
        SourceType.PACKAGE
      )))
			.flatMap( v -> Mono.from(ensureSource(
        "https://gokb.org/gokb/api",
        SourceCode.GOKB,
        SourceType.TIPP
      )))
      .subscribe();
	}

  private Publisher<Source> ensureSource(String sourceUrl, SourceCode code, SourceType sourceType) {
		return sourceService.ensureSource(sourceUrl, code, sourceType);
	}
}
