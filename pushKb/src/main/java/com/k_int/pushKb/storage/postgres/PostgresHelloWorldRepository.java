package com.k_int.pushKb.storage.postgres;

import com.k_int.pushKb.core.model.HelloWorld;
import com.k_int.pushKb.core.storage.postgres.PostgresGenericRepository;
import com.k_int.pushKb.storage.HelloWorldRepository;

@SuppressWarnings("unchecked")
public interface PostgresHelloWorldRepository extends
        PostgresGenericRepository<HelloWorld>,
        HelloWorldRepository {
}
