package com.k_int.pushKb.model;

import java.util.UUID;

public interface Destination {
  UUID getId();
  String getDestinationUrl();

  String toString();
}
