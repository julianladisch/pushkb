package com.k_int.pushKb;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.Destination;
import com.k_int.pushKb.services.DestinationService;

import com.k_int.pushKb.interactions.folio.model.FolioAuthType;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioDestinationType;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;

import com.k_int.pushKb.bootstrap.ConfigBootstrapDestinations;
import com.k_int.pushKb.bootstrap.ConfigBootstrapDestinations.ConfigBootstrapFolioDestination;
import com.k_int.pushKb.bootstrap.ConfigBootstrapDestinations.ConfigBootstrapFolioTenant;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.services.SourceService;
import com.k_int.taskscheduler.model.DutyCycleTask;
import com.k_int.taskscheduler.services.ReactiveDutyCycleTaskRunner;
import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.interactions.gokb.storage.GokbRepository;

import com.k_int.pushKb.bootstrap.ConfigBootstrapSources;
import com.k_int.pushKb.bootstrap.ConfigBootstrapSources.ConfigBootstrapGokb;
import com.k_int.pushKb.bootstrap.ConfigBootstrapSources.ConfigBootstrapGokbSource;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.services.PushableService;

import com.k_int.pushKb.bootstrap.ConfigBootstrapPushables;

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
	private final PushableService pushableService;

	// Config bootstrapping(?)
	private final ConfigBootstrapSources configBootstrapSources;
	private final ConfigBootstrapDestinations configBootstrapDestinations;
	private final ConfigBootstrapPushables configBootstrapPushables;

	// private final ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner;

	public ApplicationStartupListener(
		//ReactiveDutyCycleTaskRunner reactiveDutyCycleTaskRunner,
    Environment environment,
		DestinationService destinationService,
		SourceService sourceService,
		PushableService pushableService,
		GokbRepository gokbRepository, // This is obviously implementation specific unlike "source".
		ConfigBootstrapSources configBootstrapSources,
		ConfigBootstrapDestinations configBootstrapDestinations,
		ConfigBootstrapPushables configBootstrapPushables
 	) {
		this.environment = environment;
		this.destinationService = destinationService;
		this.sourceService = sourceService;
		this.pushableService = pushableService;
		this.configBootstrapSources = configBootstrapSources;
		this.configBootstrapDestinations = configBootstrapDestinations;
		this.configBootstrapPushables = configBootstrapPushables;

		//this.reactiveDutyCycleTaskRunner = reactiveDutyCycleTaskRunner;
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		// FIXME consider whether we really want bootstrapping at all
		log.info("Bootstrapping PushKB - onApplicationEvent");
			Flux.from(bootstrapSources())
				.thenMany(Flux.from(bootstrapDestinations()))
				.thenMany(Flux.from(bootstrapPushTasks()))
			.subscribe();

			// TODO Quick and dirty task testing -- move to test file for job runner
			// Flux.from(bootstrapTestTasks()).subscribe();

		log.info("Exit onApplicationEvent");
	}

	private GokbSource getGokbSourceFromConfig(ConfigBootstrapGokbSource configSrc) {
		// Get the requisite Gokb from bootstraps
		ConfigBootstrapGokb gkb = configBootstrapSources
			.getGokbs()
			.stream()
			.filter(g -> g.getName().equals(configSrc.getGokb()))
			.collect(Collectors.toList())
			.get(0);

		return GokbSource.builder()
			.gokb(
				Gokb.builder()
					.baseUrl(gkb.getUrl())
					.name(gkb.getName())
					.build()
			)
			.name(configSrc.getName())
			.gokbSourceType(configSrc.getType().equals("PACKAGE") ? GokbSourceType.PACKAGE : GokbSourceType.TIPP)
			.build();
	}

	private Publisher<Source> bootstrapSources() {
		log.debug("bootstrapSources");
		//log.info("CONFIG SOURCES: {}", configBootstrapSources);

		// TODO this is obviously quite domain specific -- if we ever want this in prod maybe move it to GOKB class type and have that worked into bootstrapping
		return Flux.fromIterable(configBootstrapSources.getGokbsources()).concatMap(src -> {
			try {
				GokbSource gkbSource = getGokbSourceFromConfig(src);
				UUID srcId = GokbSource.generateUUIDFromSource(gkbSource);

				return Mono.from(sourceService.ensureSource(gkbSource));
			} catch (Exception e) {
				log.error("ERROR BOOTSTRAPPING SRC {}", src, e);
				//e.printStackTrace();
				return Mono.empty();
			}
		});
	}

	private FolioDestination getFolioDestinationFromConfig(ConfigBootstrapFolioDestination configDest) {
		// Get the requisite foliotenant from bootstraps
		ConfigBootstrapFolioTenant ten = configBootstrapDestinations
			.getFoliotenants()
			.stream()
			.filter(t -> t.getName().equals(configDest.getFoliotenant()))
			.collect(Collectors.toList())
			.get(0);

		return FolioDestination.builder()
			.folioTenant(
				FolioTenant.builder()
					// There may be more auth types later
					.authType(ten.getAuthtype().equals("OKAPI") ? FolioAuthType.OKAPI : FolioAuthType.NONE)
					.baseUrl(ten.getBaseurl())
					.tenant(ten.getTenant())
					.name(ten.getName())
					.loginUser(ten.getUser())
					.loginPassword(ten.getPassword())
					.build()
			)
			.name(configDest.getName())
			.destinationType(configDest.getDestinationtype().equals("PACKAGE") ? FolioDestinationType.PACKAGE : FolioDestinationType.PCI)
			.build();
	}

	private Publisher<Destination> bootstrapDestinations() {
		log.debug("bootstrapDestinations");
		//log.info("CONFIG DESTINATIONS: {}", configBootstrapDestinations);
		
		// TODO this is obviously quite domain specific -- if we ever want this in prod maybe move it to GOKB class type and have that worked into bootstrapping
		return Flux.fromIterable(configBootstrapDestinations.getFoliodestinations()).concatMap(dest -> {
			try {
				return destinationService.ensureDestination(getFolioDestinationFromConfig(dest));
			} catch (Exception e) {
				log.error("ERROR BOOTSTRAPPING DEST {}", dest, e);
				//e.printStackTrace();
				return Mono.empty();
			}
		});
	}

	private Publisher<PushTask> bootstrapPushTasks() {
		log.debug("bootstrapPushTasks");
		//log.info("CONFIG PUSHABLES: {}", configBootstrapPushables);
		return Flux.fromIterable(configBootstrapPushables.getPushtasks()).concatMap(pt -> {
			try {
				ConfigBootstrapGokbSource configGokbSource = configBootstrapSources
					.getGokbsources()
					.stream()
					.filter(s  -> s.getName().equals(pt.getSource()))
					.collect(Collectors.toList())
					.get(0);

				GokbSource src = getGokbSourceFromConfig(configGokbSource);

				ConfigBootstrapFolioDestination configFolioDestination = configBootstrapDestinations
					.getFoliodestinations()
					.stream()
					.filter(d  -> d.getName().equals(pt.getDestination()))
					.collect(Collectors.toList())
					.get(0);

				FolioDestination dest = getFolioDestinationFromConfig(configFolioDestination);

				PushTask pushTaskObj = PushTask.builder()
					.transform(pt.getTransform())
					.sourceType(GokbSource.class)
					.sourceId(GokbSource.generateUUIDFromSource(src))
					.destinationType(FolioDestination.class)
					.destinationId(FolioDestination.generateUUIDFromDestination(dest))
					.build();

				return Mono.from(pushableService.ensurePushable(pushTaskObj)).map(PushTask.class::cast);
			} catch (Exception e) {
				log.error("ERROR BOOTSTRAPPING PUSHTASK {}", pt, e);
				//e.printStackTrace();
				return Mono.empty();
			}
		});
	}

	// TODO this should be commented out (or gone) for prod -- move to test file
	/* private Publisher<DutyCycleTask> bootstrapTestTasks() {
		log.debug("bootstrapPushTasks");
		return Flux.fromStream(IntStream.range(0, 100).boxed())
			.flatMap(ind -> {
				return reactiveDutyCycleTaskRunner.registerTask(
					Long.valueOf(1000*60),
					String.join(": ", "index", ind.toString()),
					"ReactiveTestTask",
					Map.of("index", ind)
				);
			});
	} */
}
