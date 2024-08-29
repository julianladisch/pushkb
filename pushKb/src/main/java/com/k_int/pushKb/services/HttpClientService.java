package com.k_int.pushKb.services;

import java.net.MalformedURLException;
import java.net.URL;

import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Singleton;

/* Small utility service to ensure that we're always creating a low level httpClient with the default properties specified in the application.yml */
@Singleton
public class HttpClientService {
  	// make sure we pull in config from application yml (Namely making sure max content > 10mb)
	private final DefaultHttpClientConfiguration httpConfig;
  public HttpClientService(
		DefaultHttpClientConfiguration httpConfig
  ) {
		this.httpConfig = httpConfig;
	}

  public HttpClient create(URL url) {
    return HttpClient.create(url, httpConfig);
  }

  public HttpClient create(String urlString) throws MalformedURLException {
    URL url = new URL(urlString);
    return HttpClient.create(url, httpConfig);
  }
}
