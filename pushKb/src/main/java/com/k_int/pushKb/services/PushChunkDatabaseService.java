package com.k_int.pushKb.services;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.model.PushChunk;
import com.k_int.pushKb.storage.PushChunkRepository;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;


@Singleton
public class PushChunkDatabaseService {
  private final PushChunkRepository pushChunkRepository;
	public PushChunkDatabaseService(
    PushChunkRepository pushChunkRepository
  ) {
    this.pushChunkRepository = pushChunkRepository;
	}

  @Transactional
  public Publisher<PushChunk> save (PushChunk pc) {
    return pushChunkRepository.save(pc);
  }
}