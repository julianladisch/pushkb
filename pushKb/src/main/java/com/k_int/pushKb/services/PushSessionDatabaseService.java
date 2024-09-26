package com.k_int.pushKb.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.PushSession;
import com.k_int.pushKb.storage.PushSessionRepository;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;


@Singleton
public class PushSessionDatabaseService {
  private final PushSessionRepository pushSessionRepository;
	public PushSessionDatabaseService(
    PushSessionRepository pushSessionRepository
  ) {
    this.pushSessionRepository = pushSessionRepository;
	}

  @Transactional
  public Publisher<PushSession> save (PushSession ps) {
    return pushSessionRepository.save(ps);
  }
}