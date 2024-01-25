package com.k_int.pushKb;

import java.util.Collection;
import java.util.List;
import java.lang.reflect.Field;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.destinations.folio.FolioDestination;
import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.DestinationSourceLink;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.model.SourceType;
import com.k_int.pushKb.services.DestinationService;
import com.k_int.pushKb.services.DestinationSourceLinkService;
import com.k_int.pushKb.services.SourceService;
import com.k_int.pushKb.storage.SourceRepository;
import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
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
/*   private final SourceService sourceService;
	private final DestinationSourceLinkService destinationSourceLinkService; */
 
	private final BeanContext beanContext;

  private static final String BOOSTRAP_SOURCE_VAR = "BOOTSTRAP_SOURCES";
  private static final String BOOSTRAP_DESTINATION_VAR = "BOOSTRAP_DESTINATIONS";

	public ApplicationStartupListener(
    Environment environment,
		/* SourceService sourceService,
		DestinationSourceLinkService destinationSourceLinkService, */
		BeanContext beanContext
  ) {
		this.environment = environment;
		this.beanContext = beanContext;

		/* this.sourceService = sourceService;
		this.destinationSourceLinkService = destinationSourceLinkService; */
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		log.info("Bootstrapping PushKB - onApplicationEvent");
    // FIXME this should use the BOOSTRAP_SOURCE_VAR, not always bootstrap, turned off for now for dev purposes
		/* if (environment.getProperty(BOOSTRAP_SOURCE_VAR, String.class)
				.orElse("false").equalsIgnoreCase("true")) { */

		// FIXME what is happening???
		Flux.from(bootstrapDestinations())
			
			/* Flux.from(bootstrapSources())
				.thenMany(Flux.from(bootstrapDestinations()))
				//.thenMany(Flux.from(bootstrapDestinationSourceLinks())) // FIXME PushTasks back in later */
			.subscribe();
		//}

		log.info("Exit onApplicationEvent");
	}

/* 	private Publisher<Source> bootstrapSources() {
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
	} */

	//private Publisher<Destination> bootstrapDestinations() {
	private Publisher<String> bootstrapDestinations() {
		log.debug("bootstrapDestinations");
		// Fetch all Destination Services
		// FIXME is this being raw an issue?
		Collection<BeanRegistration<DestinationService>> destinationServices = beanContext.getBeanRegistrations(DestinationService.class);
		for (BeanRegistration<DestinationService> ds : destinationServices) {
			ds.getBean();
		}
		
		log.info("SHOW ME BEAN CONTEXT: {}");

		return Flux.just("start").doOnNext(nxt -> log.info("Next up: {}", nxt));
	
		/* return Flux.fromArray(Boostraps.Destinations.class.getFields())
			.flatMap(dest -> {
				try {
					Destination destination = (Destination) dest.get(Boostraps.Destinations.class);
					if (destination instanceof FolioDestination) {
						log.info("THIS WORKED!!");
						return Mono.from(destinationService.ensureDestination((FolioDestination) destination));
					}

					// Not a recognised implementation
					log.info("Did not recognise implementation of Destination, skipping: {}", destination);
					return Mono.empty();
					
					//log.info("Bootstrapping destination: {}", destination);
					
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			}); */
	}
/* 
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
	} */
}
