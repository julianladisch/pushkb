package com.k_int.pushKb.interactions.folio.storage;

import java.util.UUID;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.crud.ReactiveStreamsPageableRepositoryUUID5;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface FolioTenantRepository extends ReactiveStreamsPageableRepositoryUUID5<FolioTenant>{

    @NonNull
    Publisher<FolioTenant> findByBaseUrlAndTenant(String baseUrl, String tenant);
  
    Publisher<FolioTenant> listOrderByBaseUrlAndTenant();

    @Override // I don't love that this has to be overwritten in every repository.
    default UUID generateUUIDFromObject(FolioTenant obj) {
        return FolioTenant.generateUUIDFromFolioTenant(obj);
    }
}
