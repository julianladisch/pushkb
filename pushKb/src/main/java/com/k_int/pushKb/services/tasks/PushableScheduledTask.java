package com.k_int.pushKb.services.tasks;

import com.k_int.pushKb.model.Pushable;
import com.k_int.pushKb.model.TemporaryPushTask;
import com.k_int.pushKb.services.PushService;
import com.k_int.pushKb.services.PushableService;
import com.k_int.taskscheduler.services.ReactiveTask;

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

  public PushableScheduledTask(
    PushableService pushableService,
    PushService pushService
  ) {
    this.pushableService = pushableService;
    this.pushService = pushService;
  }

	public Mono<Pushable> getPushableFromInfo(Map<String,Object> info) {
		log.info("PushableScheduledTask::run({})", info);

		String pushableClass = (String) info.get("pushable_class");
		UUID pid = UUID.fromString((String) info.get("pushable"));

		try {
			Class<? extends Pushable> clazz = pushableService.getPushableClassFromString(pushableClass);

			return Mono.from(pushableService.findById(clazz, pid))
				.flatMap(pushService::runPushable);
				// Can't use :: notation for some reason

		} catch (ClassNotFoundException cnfe) {
			log.error("Something went wrong in PushableScheduledTask::getPushableFromInfo", cnfe);

			// Not sure about this, allows downstream to control instead
			return Mono.error(cnfe);
		}
	}

  public Mono<Map<String,Object>> run(Map<String,Object> info) {
    return getPushableFromInfo(info)
			.flatMap(pushable -> Mono.from(pushableService.complete(pushable))) // Should handle deletion of TemporaryPushTasks
			// FIXME we could do pointer logic etc here
			.flatMap(done -> Mono.just(info));
	}

	public Mono<Boolean> cleanupTask(Map<String,Object> info) {
		String pushableClass = (String) info.get("pushable_class");
		Class<? extends Pushable> clazz;
		try {
			clazz = pushableService.getPushableClassFromString(pushableClass);
		} catch (ClassNotFoundException cnfe) {
			log.error("Something went wrong in PushableScheduledTask::cleanupTask", cnfe);

			// Not sure about this, allows downstream to control instead
			return Mono.error(cnfe);
		}

		if (clazz == TemporaryPushTask.class) {
			return Mono.just(true);
		}

		return Mono.just(false);
	}
}
