package com.k_int.pushKb.destinations.folio;

import com.k_int.pushKb.services.DestinationApiService;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Optional;

import com.k_int.pushKb.interactions.HttpClientRequestResponseException;
import com.k_int.pushKb.interactions.folio.FolioApiClient;
import com.k_int.pushKb.interactions.folio.FolioLoginError;

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

 // Return Mono with TRUE if we successfully fetched or FALSE if we didn't (For whatever reason)
 // This isn't really the best logic... stream with True/False only works in DCB cos they expect a true/false from the validate check...
 // onErrorResume is a _fallback_
 // TODO sort out how this -should_ return *shrug*
	public Mono<Boolean> testMethod(FolioDestination destination) {
		FolioApiClient folioClient = getFolioClient(destination);
		return Mono.from(folioClient.getAgreements())
				// Remove any errors on successful fetch
				.doOnNext(resp -> {
					log.info("WHAT IS RESP? {}", resp);
				})
				.flatMap(resp -> Mono.just(Boolean.TRUE))
				.doOnError(HttpClientException.class, hce -> {
					if (hce.getCause() instanceof ConnectException) {
						log.error("Failed to connect to destination ({}). Connection error: {}", destination.getId(), hce.getCause().getMessage());
					} else if (hce instanceof HttpClientRequestResponseException) {
						// Our special caught error.
						HttpClientRequestResponseException hcrre = (HttpClientRequestResponseException) hce;
						// Login specific logging
						if (hcrre.getRequest().getPath().equals(FolioApiClient.LOGIN_URI)) {
							Optional<FolioLoginError> fle = hcrre.getResponse().getBody(FolioLoginError.class);
							if (fle.isEmpty()) {
								log.error("Failed to login to destination ({})", destination.getId());
							} else {
								log.error("Failed to login to destination ({}). {}", destination.getId(), fle.get().getErrors().get(0).getMessage());
							}
						} else {
							log.error("Something went wrong connecting to destination ({}) on path {}. {}", destination.getId(), hcrre.getRequest().getPath(), hcrre.getMessage());
						}
					}
				})
				// TODO this probably isn't right, but it'll do for now
				.onErrorResume(HttpClientException.class, hce -> Mono.just(Boolean.FALSE));
	}
}