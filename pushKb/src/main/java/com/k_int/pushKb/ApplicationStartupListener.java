package com.k_int.pushKb;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.services.DestinationService;
import com.k_int.pushKb.services.PushTaskService;
import com.k_int.pushKb.services.SourceService;

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
	private final DestinationService destinationService;
  private final SourceService sourceService;
	private final PushTaskService pushTaskService;
 
  private static final String BOOSTRAP_SOURCES_VAR = "BOOSTRAP_SOURCES";
  private static final String BOOSTRAP_DESTINATIONS_VAR = "BOOSTRAP_DESTINATIONS";
  private static final String BOOSTRAP_PUSH_TASKS_VAR = "BOOSTRAP_PUSH_TASKS";

	public ApplicationStartupListener(
    Environment environment,
		DestinationService destinationService,
		SourceService sourceService,
		PushTaskService pushTaskService
 	) {
		this.environment = environment;
		this.destinationService = destinationService;
		this.sourceService = sourceService;
		this.pushTaskService = pushTaskService;
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		log.info("Bootstrapping PushKB - onApplicationEvent");
    // FIXME this should use the BOOSTRAP_SOURCE_VAR, not always bootstrap, turned off for now for dev purposes
		/* if (environment.getProperty(BOOSTRAP_SOURCE_VAR, String.class)
				.orElse("false").equalsIgnoreCase("true")) { */

			Flux.from(bootstrapSources())
				.thenMany(Flux.from(bootstrapDestinations()))
				.thenMany(Flux.from(bootstrapPushTasks()))
			.subscribe();
		//}

		log.info("Exit onApplicationEvent");
	}

	private Publisher<Source> bootstrapSources() {
		log.debug("bootstrapSources");
		return Flux.fromIterable(Boostraps.sources.values())
			.flatMap(src -> {
				try {
					return sourceService.ensureSource(src, src.getClass());
					
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}

	private Publisher<Destination> bootstrapDestinations() {
		log.debug("bootstrapDestinations");
		
		return Flux.fromIterable(Boostraps.destinations.values())
			.flatMap(dest -> {
				try {
					return destinationService.ensureDestination(dest, dest.getClass());
					
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}

	private Publisher<PushTask> bootstrapPushTasks() {
		log.debug("bootstrapPushTasks");
		return Flux.fromIterable(Boostraps.pushTasks.values())
			.flatMap(pt -> {
				try {
					return Mono.from(pushTaskService.ensurePushTask(pt));
				} catch (Exception e) {
					e.printStackTrace();
					return Mono.empty();
				}
			});
	}
}
