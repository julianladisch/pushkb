package com.k_int;

import com.k_int.taskscheduler.services.ReactiveTask;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import jakarta.inject.Named;

import java.time.Duration;
import java.util.Map;

// Small utility task to allow quick testing of reactive tasks -- ONLY FOR TESTING
@Slf4j
@Singleton
@Named("ReactiveTestTask")
public class ReactiveTestTask implements ReactiveTask {

  public ReactiveTestTask(
  ) {
  }

  public Mono<Map<String,Object>> run(Map<String,Object> info) {
		log.info("ReactiveTestTask::run({})", info);

    return Mono.just(info).delayElement(Duration.ofMillis(25*1000)).doOnNext(i -> { log.info("Delay completed for task {}", i.get("index")); });
	}
}
