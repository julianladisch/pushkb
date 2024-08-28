package com.k_int.pushKb.interactions;

import static io.micronaut.http.HttpMethod.GET;
import static io.micronaut.http.HttpMethod.POST;
import static io.micronaut.http.HttpMethod.PUT;
import static io.micronaut.http.HttpMethod.DELETE;

import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Consumer;
import java.util.function.Function;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import reactor.core.publisher.Mono;


public class BaseApiClient implements ApiClient {
  HttpClient client;

  public BaseApiClient(
		HttpClient client
	) {  
    this.client = client;
	}

  protected Mono<MutableHttpRequest<?>> createRequest(
    HttpMethod method,
    String path,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {
		return Mono.just(HttpRequest.create(method, path))
      .map(req -> {
        if (uriBuilderConsumer.isPresent()) {
          return req.uri(uriBuilderConsumer.get());
        }
        return req;
      })
      .map(req -> {
        if (httpHeaderConsumer.isPresent()) {
          return req.headers(httpHeaderConsumer.get());
        }
        return req;
      });
	}

  // Don't really see the point in doRetrieve, we can grab the body straight from doExchange anyway? Here as protected method in case implementors want it I guess
  protected <T> Mono<T> doRetrieve(MutableHttpRequest<?> request, Argument<T> argumentType, Optional<Boolean> mapErrors, Optional<Argument<?>> errorType) {
		var response = errorType.isPresent() ?
      Mono.from(client.retrieve(request, argumentType, errorType.get())) :
      Mono.from(client.retrieve(request, argumentType));
      
		return mapErrors.orElse(false) ? response.transform(mono -> this.handleResponseErrors(mono, request)) : response;
	}

  protected <T> Mono<HttpResponse<T>> doExchange(MutableHttpRequest<?> request, Argument<T> argumentType, Optional<Boolean> mapErrors, Optional<Argument<?>> errorType) {
		var response = errorType.isPresent() ?
      Mono.from(client.exchange(request, argumentType, errorType.get())) :
      Mono.from(client.exchange(request, argumentType));
      
		return mapErrors.orElse(false) ? response.transform(mono -> this.handleResponseErrors(mono, request)) : response;
	}

  protected <T> Mono<T> handleResponseErrors(final Mono<T> current, MutableHttpRequest<?> request) {
		return current.onErrorMap(exception -> {
			if (exception instanceof HttpClientResponseException) {
				// Return special HttpClientRequestResponseException which has access to both the exception and the original request
				return new HttpClientRequestResponseException((HttpClientResponseException)exception, request);
			}
			return exception;
		});
	}

  protected Optional<Argument<?>> getErrorArgument(Optional<Class<?>> errorType) {
    if (errorType.isPresent()) {
      return Optional.of(Argument.of(errorType.get()));
    }
  
    return Optional.empty();
  }

  public <T> Mono<HttpResponse<T>> get(
    String path,
    Class<T> responseType,
    Optional<Class<?>> errorType
  ) {
		return get(path, responseType, errorType, Optional.empty(), Optional.empty());
	}

  public <T> Mono<HttpResponse<T>> get(
    String path,
    Class<T> responseType,
    Optional<Class<?>> errorType,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {

		return get(
      createRequest(GET, path, uriBuilderConsumer, httpHeaderConsumer),
      responseType,
      errorType
    );
	}

  public <T> Mono<HttpResponse<T>> get(
    Mono<MutableHttpRequest<?>> request,
    Class<T> responseType,
    Optional<Class<?>> errorType
  ) {
    return request
      .flatMap(req -> doExchange(req, Argument.of(responseType), Optional.of(true), getErrorArgument(errorType)))
  }

  public <T> Mono<HttpResponse<T>> post(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  ) {

		return post(path, responseType, body, errorType, Optional.empty(), Optional.empty());
	}

  public <T> Mono<HttpResponse<T>> post(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {

		return post(
      createRequest(POST, path, uriBuilderConsumer, httpHeaderConsumer),
      responseType,
      body,
      errorType
    );
	}

  public <T> Mono<HttpResponse<T>> post(
    Mono<MutableHttpRequest<?>> request,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  ) {
    return request
      .map(req -> {
        if (body.isPresent()) {
          return req.body(body);
        }

        return req;
      })
      .flatMap(req -> doExchange(req, Argument.of(responseType), Optional.of(true), getErrorArgument(errorType)))
  }

  public <T> Mono<HttpResponse<T>> put(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  ) {

		return put(path, responseType, body, errorType, Optional.empty(), Optional.empty());
	}

  public <T> Mono<HttpResponse<T>> put(
    String path,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {

		return put(
      createRequest(PUT, path, uriBuilderConsumer, httpHeaderConsumer),
      responseType,
      body,
      errorType
    );
	}

  public <T> Mono<HttpResponse<T>> put(
    Mono<MutableHttpRequest<?>> request,
    Class<T> responseType,
    Optional<?> body,
    Optional<Class<?>> errorType
  ) {
    return request
      .map(req -> {
        if (body.isPresent()) {
          return req.body(body);
        }

        return req;
      })
      .flatMap(req -> doExchange(req, Argument.of(responseType), Optional.of(true), getErrorArgument(errorType)))
  }


  public <T> Mono<HttpResponse<T>> delete(
    String path,
    Class<T> responseType,
    Optional<Class<?>> errorType
  ) {
		return delete(path, responseType, errorType, Optional.empty(), Optional.empty());
	}

  public <T> Mono<HttpResponse<T>> delete(
    String path,
    Class<T> responseType,
    Optional<Class<?>> errorType,
    Optional<Consumer<UriBuilder>> uriBuilderConsumer,
    Optional<Consumer<MutableHttpHeaders>> httpHeaderConsumer
  ) {

		return delete(
      createRequest(DELETE, path, uriBuilderConsumer, httpHeaderConsumer),
      responseType,
      errorType
    );
	}

  public <T> Mono<HttpResponse<T>> delete(
    Mono<MutableHttpRequest<?>> request,
    Class<T> responseType,
    Optional<Class<?>> errorType
  ) {
    request.flatMap(req -> doExchange(req, Argument.of(responseType), Optional.of(true), getErrorArgument(errorType)))
  }
}
