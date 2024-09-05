package com.k_int.pushKb.interactions.folio.storage;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.folio.model.FolioTenant;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsPageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioTenantRepository extends ReactiveStreamsPageableRepository<FolioTenant, UUID>{
    // Unique up to baseUrl/tenant
    @NonNull
    @SingleResult
    Publisher<Boolean> existsById(@Nullable UUID id);
    
    // Unique up to baseUrl
    @NonNull
    @SingleResult
    Publisher<FolioTenant> findById(@Nullable UUID id);
  
    @NonNull
    Publisher<FolioTenant> findByBaseUrlAndTenant(String baseUrl, String tenant);
  
    Publisher<FolioTenant> listOrderByBaseUrlAndTenant();
}