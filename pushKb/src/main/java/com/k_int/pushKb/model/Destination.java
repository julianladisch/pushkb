package com.k_int.pushKb.model;

import java.util.UUID;
import java.util.List;

public interface Destination {
  UUID getId();
  String getDestinationUrl();

  String toString();

  List<? extends Error> getErrors();
}
