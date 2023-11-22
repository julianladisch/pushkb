package com.k_int.pushKb.core.generic.storage.postgres;

import java.util.UUID;

import com.k_int.pushKb.core.generic.model.Generic;

import jakarta.transaction.Transactional;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import jakarta.inject.Singleton;
import io.micronaut.data.repository.jpa.reactive.ReactiveStreamsJpaSpecificationExecutor;

@Singleton
@R2dbcRepository(dialect = Dialect.POSTGRES)
@Transactional
public interface PostgresGenericRepository<T extends Generic> extends 
        ReactiveStreamsPageableRepository<T, UUID>, 
        ReactiveStreamsJpaSpecificationExecutor<T> {
}
