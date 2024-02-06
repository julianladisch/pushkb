package com.k_int.pushKb.destinations.folio;

import com.k_int.pushKb.services.DestinationApiService;

import io.micronaut.http.client.HttpClient;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.k_int.pushKb.interactions.folio.FolioApiClient;

@Singleton
@Slf4j
public class FolioDestinationApiService implements DestinationApiService<FolioDestination> {
	private final HttpClient client;

	public FolioDestinationApiService(
		HttpClient client
	) {  
			this.client = client;
	}

	// Be able to set up a new FolioApiClient per destination
	FolioApiClient getFolioClient(FolioDestination destination) {
		return new FolioApiClient(
			client,
			destination.getDestinationUrl(),
			destination.getTenant(),
			destination.getLoginUser(),
			destination.getLoginPassword()
		);
	}

	public void testMethod(FolioDestination destination) {
		FolioApiClient folioClient = getFolioClient(destination);
		Mono.from(folioClient.getAgreements())
				.doOnNext(resp -> log.info("WHAT IS RESP? {}", resp))
				.subscribe();
	}
}