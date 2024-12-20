package com.k_int.pushKb.services.tasks;

import com.k_int.pushKb.model.Source;
import com.k_int.pushKb.services.SourceService;
import com.k_int.taskscheduler.services.ReactiveTask;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import jakarta.inject.Named;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Singleton
@Named("IngestScheduledTask")
public class IngestScheduledTask implements ReactiveTask {
  private final SourceService sourceService;
  private final ApplicationContext applicationContext;

  public IngestScheduledTask(
    SourceService sourceService,
    ApplicationContext applicationContext
  ) {
    this.sourceService = sourceService;
    this.applicationContext = applicationContext;
  }

  // FIXME should this be Publisher?
  public Mono<Map<String,Object>> run(Map<String,Object> info) {
    ClassLoader classLoader = applicationContext.getClassLoader();

		log.info("IngestScheduledTask::run({})", info);

    String sourceClass = (String) info.get("source_class");
    UUID pid = UUID.fromString((String) info.get("source"));

    // FIXME This is unchecked type conversion :/
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Source> clazz = (Class<? extends Source>) classLoader.loadClass(sourceClass);

      return Mono.from(sourceService.findById(clazz, pid))
              // Can't use :: notation for some reason
        .flatMap(source -> Mono.from(sourceService.triggerIngestForSource(source)))
        .flatMap(source -> {
          // We're done at this point with no data tro update, so return info as we were handed it
          return Mono.just(info);
        });
    
    } catch (ClassNotFoundException cnfe) {
      log.error("Something went wrong in IngestScheduledTask::run", cnfe);

      // Not sure about this, allows downstream to control instead
      return Mono.error(cnfe);
    }
	}
}
