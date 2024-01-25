package com.k_int.pushKb.destinations.folio;

import com.k_int.pushKb.storage.DestinationRepository;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioDestinationRepository extends DestinationRepository<FolioDestination> {}
