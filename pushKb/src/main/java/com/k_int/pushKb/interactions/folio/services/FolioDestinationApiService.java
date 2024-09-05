package com.k_int.pushKb.interactions.folio.services;

import com.k_int.pushKb.services.DestinationApiService;
import com.k_int.pushKb.services.HttpClientService;

import io.micronaut.http.client.HttpClient;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

import org.reactivestreams.Publisher;

import com.k_int.pushKb.interactions.DestinationClient;
import com.k_int.pushKb.interactions.folio.FolioApiClient;
import com.k_int.pushKb.interactions.folio.model.FolioDestination;
// import com.k_int.pushKb.interactions.folio.model.FolioLoginError;

@Singleton
@Slf4j
public class FolioDestinationApiService implements DestinationApiService<FolioDestination> {
	private final HttpClientService httpClientService;

	public FolioDestinationApiService(
		HttpClientService httpClientService
	) {
		this.httpClientService = httpClientService;
	}

	// Does this belong in some other service, or perhaps should "getClient" be a method on DestinationApiService interface?
	// Be able to set up a new FolioApiClient per destination
	public Publisher<FolioApiClient> getClient(FolioDestination destination) {
		try {
			HttpClient client = httpClientService.create(destination.getDestinationUrl());
			return Mono.just(new FolioApiClient(
				client,
				destination.getFolioTenant(),
				destination.getDestinationType()
			));
		} catch (MalformedURLException e) {
			return Mono.error(e);
		}
	}

	// Folio destination error handling (in doOnError... not sure this is right yet)
	/* private Consumer<HttpClientException> getErrorHandler(FolioApiClient client) {
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
	} */

	// WIP... I'm not sure about the return shape here
	// Should we be passing destintation in here?
	public Mono<Boolean> push(FolioDestination destination, DestinationClient<FolioDestination> client, JsonNode json) {
		// FIXME Not thrilled about this cast, but can't figure out the typing to do this directly
		if (client instanceof FolioApiClient) {
			FolioApiClient folioClient = (FolioApiClient) client;

			return Mono.from(folioClient.pushPCIs(json))
			.doOnNext(resp -> {
				log.info("WHAT IS RESP? {}", resp);
			})
			.flatMap(string -> Mono.just(Boolean.TRUE));
			//.doOnError(HttpClientException.class, getErrorHandler(client));
			// TODO this probably isn't right, but it'll do for now
			//.onErrorResume(HttpClientException.class, hce -> Mono.just(Boolean.FALSE));
		} else {
			return Mono.error(new IllegalArgumentException("Client must be a FolioApiClient"));
		}
	}
}