package com.k_int.pushKb.services;

import com.k_int.pushKb.model.Destination;

// Generic interface for all interactions with the API for a give destination type
public interface DestinationApiService<T extends Destination> {
  void testMethod(T destination);
}
