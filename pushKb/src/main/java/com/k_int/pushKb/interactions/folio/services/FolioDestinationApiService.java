package com.k_int.pushKb.interactions.folio.services;

import com.k_int.pushKb.services.DestinationApiService;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import com.k_int.pushKb.interactions.HttpClientRequestResponseException;
import com.k_int.pushKb.interactions.folio.FolioApiClient;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioLoginError;

@Singleton
@Slf4j
public class FolioDestinationApiService implements DestinationApiService<FolioDestination> {
	public FolioDestinationApiService() {
	}

	// Does this belong in some other service, or perhaps should "getClient" be a method on DestinationApiService interface?
	// Be able to set up a new FolioApiClient per destination
	FolioApiClient getFolioClient(FolioDestination destination) throws MalformedURLException {
		URL url = new URL(destination.getDestinationUrl());
		HttpClient client = HttpClient.create(url);

		return new FolioApiClient(
			client,
			destination.getTenant(),
			destination.getLoginUser(),
			destination.getLoginPassword()
		);
	}

	// Folio destination error handling (in doOnError... not sure this is right yet)
	private Consumer<HttpClientException> getErrorHandler(FolioDestination destination) {
		return hce -> {
			if (hce.getCause() instanceof ConnectException) {
				log.error("Failed to connect to destination ({}). Connection error: {}", destination.getId(), hce.getCause().getMessage());
			} else if (hce instanceof HttpClientRequestResponseException) {
				// Our special caught error.
				HttpClientRequestResponseException hcrre = (HttpClientRequestResponseException) hce;
				// Login specific logging
				if (hcrre.getRequest().getPath().equals(FolioApiClient.LOGIN_URI)) {
					Optional<FolioLoginError> fle = hcrre.getResponse().getBody(FolioLoginError.class);
					if (fle.isEmpty()) {
						log.error("Failed to login to destination ({}). {}", destination.getId(), hcrre.getMessage());
					} else {
						log.error("Failed to login to destination ({}). {}", destination.getId(), fle.get().getErrors().get(0).getMessage());
					}
				} else {
					log.error("Something went wrong connecting to destination ({}) on path {}. {}", destination.getId(), hcrre.getRequest().getPath(), hcrre.getMessage());
				}
			}
		};
	}

	// Return Mono with TRUE if we successfully fetched or FALSE if we didn't (For whatever reason)
	// This isn't really the best logic... stream with True/False only works in DCB cos they expect a true/false from the validate check...
	// onErrorResume is a _fallback_
	// TODO sort out how this -should_ return *shrug*
	public Mono<Boolean> testMethod(FolioDestination destination) {
		try {
			FolioApiClient folioClient = getFolioClient(destination);

			return Mono.from(folioClient.getAgreements())
				// Remove any errors on successful fetch
				.doOnNext(resp -> {
					log.info("WHAT IS RESP? {}", resp);
				})
				.flatMap(resp -> Mono.just(Boolean.TRUE))
				.doOnError(HttpClientException.class, getErrorHandler(destination))
				// TODO this probably isn't right, but it'll do for now
				.onErrorResume(HttpClientException.class, hce -> Mono.just(Boolean.FALSE));
		} catch (MalformedURLException e) {
			return Mono.error(e);
		}
	}
}