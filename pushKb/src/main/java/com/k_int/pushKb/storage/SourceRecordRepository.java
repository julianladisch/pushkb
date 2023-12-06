package com.k_int.pushKb.storage;

import com.k_int.pushKb.model.SourceRecord;

import java.util.UUID;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;

import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional
public interface SourceRecordRepository extends ReactiveStreamsPageableRepository<SourceRecord, UUID> {
}