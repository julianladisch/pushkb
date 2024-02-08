package com.k_int.pushKb.interactions;

import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.Getter;

public class HttpClientRequestResponseException extends HttpClientResponseException {
  @Getter
  MutableHttpRequest<?> request;

  public HttpClientRequestResponseException(HttpClientResponseException superException, MutableHttpRequest<?> request) {
    super(superException.getMessage(), superException.getResponse());
    this.request = request;
  }
}
