package com.k_int.pushKb;

import java.util.Collection;
import java.util.List;
import java.lang.reflect.Field;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;
import com.k_int.pushKb.model.DestinationType;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceCode;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.services.DestinationService;
import com.k_int.pushKb.services.DestinationSourceLinkService;
import com.k_int.pushKb.services.SourceService;
import com.k_int.pushKb.storage.SourceRepository;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Singleton
public class ApplicationStartupListener implements ApplicationEventListener<StartupEvent> {
	private final Environment environment;
  private final SourceService sourceService;
	private final DestinationService destinationService;
	private final DestinationSourceLinkService destinationSourceLinkService;

  private static final String BOOSTRAP_SOURCE_VAR = "BOOTSTRAP_SOURCES";
  private static final String BOOSTRAP_DESTINATION_VAR = "BOOSTRAP_DESTINATIONS";

	public ApplicationStartupListener(
    Environment environment,
		SourceService sourceService,
		DestinationService destinationService,
		DestinationSourceLinkService destinationSourceLinkService
  ) {
		this.environment = environment;
		this.sourceService = sourceService;
		this.destinationService = destinationService;
		this.destinationSourceLinkService = destinationSourceLinkService;
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		log.info("Bootstrapping PushKB - onApplicationEvent");
    // FIXME this should use the BOOSTRAP_SOURCE_VAR, not always bootstrap, turned off for now for dev purposes
		/* if (environment.getProperty(BOOSTRAP_SOURCE_VAR, String.class)
				.orElse("false").equalsIgnoreCase("true")) { */
			Flux.from(bootstrapSources())
				.thenMany(Flux.from(bootstrapDestinations()))
				.thenMany(Flux.from(bootstrapDestinationSourceLinks()))
			.subscribe();
		//}

		log.info("Exit onApplicationEvent");
	}

	private Publisher<Source> bootstrapSources() {
		log.debug("bootstrapSources");
		return Flux.fromArray(Boostraps.Sources.class.getFields())
			.flatMap(src -> {
				try {
					Source source = (Source) src.get(Boostraps.Sources.class);
					//log.info("Bootstrapping source: {}", source);
					return Mono.from(sourceService.ensureSource(source));
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}

	private Publisher<Destination> bootstrapDestinations() {
		log.debug("bootstrapDestinations");
		return Flux.fromArray(Boostraps.Destinations.class.getFields())
			.flatMap(dest -> {
				try {
					Destination destination = (Destination) dest.get(Boostraps.Destinations.class);
					//log.info("Bootstrapping destination: {}", destination);
					return Mono.from(destinationService.ensureDestination(destination));
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}

	private Publisher<DestinationSourceLink> bootstrapDestinationSourceLinks() {
		log.debug("bootstrapDestinationSourceLinks");
		return Flux.fromArray(Boostraps.DestinationSourceLinks.class.getFields())
			.flatMap(dsl -> {
				try {
					DestinationSourceLink destinationSourceLink = (DestinationSourceLink) dsl.get(Boostraps.DestinationSourceLinks.class);
					//log.info("Bootstrapping destination source link: {}", destinationSourceLink);
					return Mono.from(destinationSourceLinkService.ensureDestinationSourceLink(destinationSourceLink));
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}
}
