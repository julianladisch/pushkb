package com.k_int.pushKb.destinations.folio;

import com.k_int.pushKb.services.DestinationApiService;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

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

	// TODO right now these will never be removed on success
	// COnnect exceptions and login exceptions need errors adding to the destination itself, so we can look those up directly
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

	public Mono<FolioDestinationError> handleConnectError(FolioDestination destination, ConnectException ce) {
		return Mono.from(folioDestinationErrorRepository.existsByOwnerAndCode(destination, FolioDestinationError.CONNECT_ERROR_CODE))
				.flatMap(exists -> {
					if (!exists) {
						String message = "Failed to connect: " + ce.getMessage();
						return Mono.from(folioDestinationErrorRepository.addError(destination, FolioDestinationError.CONNECT_ERROR_CODE, message));
					}
					return Mono.empty();
				});
	}

	public Mono<FolioDestinationError> handleErrors(HttpClientException hce, FolioDestination destination) {
		log.info("HANDLE ERRORS: {}", hce);

		// Special case for connectException
		if (hce.getCause() instanceof ConnectException) {
			// Something went wrong connecting to destination
			return handleConnectError(destination, (ConnectException) hce.getCause());
		}

		// TODO We should remove connect exceptions under here ?

		if (
			hce instanceof HttpClientRequestResponseException &&
			((HttpClientRequestResponseException) hce).getRequest().getPath().equals(FolioApiClient.LOGIN_URI)
		) {
			return handleLoginError(destination, (HttpClientRequestResponseException) hce);
		}

		return Mono.empty();
	}

	public Mono<Void> handleRemoveConnectErrors(FolioDestination destination) {
		return Mono.from(folioDestinationErrorRepository.deleteAllByOwnerAndCode(destination, FolioDestinationError.CONNECT_ERROR_CODE));
	}

	public Mono<Void> handleRemoveLoginErrors(FolioDestination destination) {
		return Mono.from(folioDestinationErrorRepository.deleteAllByOwnerAndCode(destination, FolioDestinationError.LOGIN_ERROR_CODE));
	}

	public <T> Mono<T> handleRemoveErrors(T incoming, FolioDestination destination) {
		// Remove all login and connect exceptions once we've successfully connected
		log.info("HANDLE REMOVE ERRORS");
		return handleRemoveConnectErrors(destination)
							 .then(handleRemoveLoginErrors(destination))
							 .then(Mono.just(incoming));
	}

 // Return Mono with TRUE if we successfully fetched or FALSE if we didn't (For whatever reason)
 // This isn't really the best logic... stream with True/False only works in DCB cos they expect a true/false from the validate check...
 // onErrorResume is a _fallback_
 // TODO sort out how this -should_ return *shrug*
	public Mono<Boolean> testMethod(FolioDestination destination) {
		FolioApiClient folioClient = getFolioClient(destination);
		return Mono.from(folioClient.getChunks())
				// Remove any errors on successful fetch
				.flatMap(resp -> {
					log.info("WHAT IS RESP? {}", resp);
					return this.handleRemoveErrors(resp, destination);
				})
				.flatMap(resp -> Mono.just(Boolean.TRUE))
				.onErrorResume(HttpClientException.class, hce -> {
					return this.handleErrors(hce, destination).then(Mono.just(Boolean.FALSE));
				});
		
	}
}