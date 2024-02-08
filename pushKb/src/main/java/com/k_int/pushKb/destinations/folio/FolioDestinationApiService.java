package com.k_int.pushKb.destinations.folio;

import com.k_int.pushKb.services.DestinationApiService;

import io.micronaut.http.client.HttpClient;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.k_int.pushKb.interactions.HttpClientRequestResponseException;
import com.k_int.pushKb.interactions.folio.FolioApiClient;
import com.k_int.pushKb.interactions.folio.FolioLoginError;

@Singleton
@Slf4j
public class FolioDestinationApiService implements DestinationApiService<FolioDestination> {
	private final HttpClient client;
	private final FolioDestinationErrorRepository folioDestinationErrorRepository;

	public FolioDestinationApiService(
		HttpClient client,
		FolioDestinationErrorRepository folioDestinationErrorRepository
	) {  
		this.client = client;
		this.folioDestinationErrorRepository = folioDestinationErrorRepository;
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

	public Mono<FolioDestinationError> handleLoginError(FolioDestination destination, HttpClientRequestResponseException fre) {
		return Mono.from(folioDestinationErrorRepository.existsByOwnerAndCode(destination, FolioDestinationError.LOGIN_ERROR_CODE))
				.flatMap(exists -> {
					FolioLoginError loginErrorBody = fre.getResponse().getBody(FolioLoginError.class).get();
					if (!exists) {
						String message = "Failed to log in. " + fre.getResponse().getStatus() + ": " + loginErrorBody.getErrors().get(0).getMessage();
						return Mono.from(folioDestinationErrorRepository.addError(destination, FolioDestinationError.LOGIN_ERROR_CODE, message));
					}
					return Mono.empty();
				});
	}

 // Return Mono with TRUE if we successfully fetched or FALSE if we didn't (For whatever reason)
 // This isn't really the best logic... stream with True/False only works in DCB cos they expect a true/false from the validate check...
 // onErrorResume is a _fallback_
 // TODO sort out how this -should_ return *shrug*
	public Mono<Boolean> testMethod(FolioDestination destination) {
		FolioApiClient folioClient = getFolioClient(destination);
		return Mono.from(folioClient.getAgreements())
				.doOnNext(resp -> log.info("WHAT IS RESP? {}", resp))
				.flatMap(resp -> Mono.just(Boolean.TRUE))
				.onErrorResume(HttpClientRequestResponseException.class, fre -> {
					if (fre.getRequest().getPath().equals(FolioApiClient.LOGIN_URI)) {
						//log.info("We had an issue logging in");
						return handleLoginError(destination, fre).flatMap(fde -> Mono.just(Boolean.FALSE));
					}

					return Mono.just(Boolean.FALSE);
				});
	}
}