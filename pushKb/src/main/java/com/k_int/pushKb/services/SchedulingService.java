package com.k_int.pushKb.services;

import java.util.Arrays;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.model.TemporaryPushTask;

@Singleton
@Slf4j
public class SchedulingService {
	private final SourceService sourceService;
	private final PushableService pushableService;
	private final PushService pushService;


	// These are here to make sure that if this is running an ingest or a push
	// we don't try to trigger another one, eg if the scheduler refires before finishing
	// We'll use a single pushRunner for both temporary AND regular pushTasks for now
	private Disposable pushTaskRunnerDisposable;
	private Disposable ingestRunnerDisposable;

	public SchedulingService(
		SourceService sourceService,
		PushableService pushableService,
		PushService pushService
	) {
		this.sourceService = sourceService;
		this.pushableService = pushableService;
		this.pushService = pushService;
	}

	public void resetPushRunner() {
		pushTaskRunnerDisposable = null;
	}

	public void resetIngestRunner() {
		ingestRunnerDisposable = null;
	}

	// Run 30 mins out of sync with ingest -- idea is that it shouldn't care if things are already running etc.
	@Scheduled(initialDelay = "30m", fixedDelay = "1h")
	//@Scheduled(initialDelay = "2m", fixedDelay = "1h")
	public void pushRunner() {
		if (pushTaskRunnerDisposable == null) {
			// Iterate over all PushTasks, maybe want to be smarter about this in future
			// STrat with just temporary?
			pushTaskRunnerDisposable = Flux.fromIterable(Arrays.asList(TemporaryPushTask.class, PushTask.class))
				.flatMapSequential(pushableService::getFeed) // SHOULD ensure that TemporaruPushTasks are run first.
				.concatMap(pushService::runPushable)
				.flatMap(pushableService::complete) // Consumes a Pushable, returns a boolean, should be the last step
				.subscribe(
					b -> {}, // Consumer (doOnNext)
					e -> {
						log.error("Something went wrong in pushTaskScheduler: {}", e);
						resetPushRunner();
					}, // errorConsumer (doOnError)
					() -> {
						log.info("pushRunner complete");
						resetPushRunner();
					} // completeConsumer (doOnComplete)
				);
		} else {
			log.warn("Pushes in progress, skipping");
		}
	}


  @Scheduled(initialDelay = "1s", fixedDelay = "1h")
	public void ingestRunner() {
		if (ingestRunnerDisposable == null) {
			// Fetch all source implementers from sourceService
			ingestRunnerDisposable = Flux.from(sourceService.getSourceImplementors())
				// For each class implementing Source, list all actual Sources in DB
				// PushTasks may rely on whether these have completed or not...
				.flatMap(sourceService::list)
				// For each source, trigger an ingest of all records
				.concatMap(sourceService::triggerIngestForSource) // OUTPUT is final saved source.
				.subscribe(
					sr -> {}, // Consumer (doOnNext)
					e -> {
						log.error("Something went wrong in ingestScheduler: {}", e);
						resetIngestRunner();
					}, // errorConsumer (doOnError)
					() -> resetIngestRunner() // completeConsumer (doOnComplete)
				);
		} else {
			log.warn("Ingest in progress, skipping");
		}
	}
}
