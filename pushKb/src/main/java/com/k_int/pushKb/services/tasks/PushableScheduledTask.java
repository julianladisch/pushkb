package com.k_int.pushKb.services.tasks;

import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.services.PushService;
import com.k_int.pushKb.services.PushableService;
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
@Named("PushableScheduledTask")
public class PushableScheduledTask implements ReactiveTask {
  private final PushableService pushableService;
  private final PushService pushService;
  private final ApplicationContext applicationContext;

  public PushableScheduledTask(
    PushableService pushableService,
    PushService pushService,
    ApplicationContext applicationContext
  ) {
    this.pushableService = pushableService;
    this.pushService = pushService;
    this.applicationContext = applicationContext;
  }

  public Mono<Map<String,Object>> run(Map<String,Object> info) {
    ClassLoader classLoader = applicationContext.getClassLoader();

		log.info("PushableScheduledTask::run({})", info);

    String pushableClass = (String) info.get("pushable_class");
    UUID pid = UUID.fromString((String) info.get("pushable"));

    // FIXME This is unchecked type conversion :/
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Pushable> clazz = (Class<? extends Pushable>) classLoader.loadClass(pushableClass);

      return Mono.from(pushableService.findById(clazz, pid))
        .flatMap(pushService::runPushable)
        // Can't use :: notation for some reason
        .flatMap(pushable -> Mono.from(pushableService.complete(pushable))) // Should handle deletion of TemporaryPushTasks
        // FIXME we could do pointer logic etc here
        .flatMap(done -> Mono.just(info));
    
    } catch (ClassNotFoundException cnfe) {
      log.error("Something went wrong in PushableScheduledTask::run", cnfe);

      // Not sure about this, allows downstream to control instead
      return Mono.error(cnfe);
    }
	}
}
