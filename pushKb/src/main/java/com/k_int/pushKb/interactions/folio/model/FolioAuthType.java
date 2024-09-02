package com.k_int.pushKb.interactions.folio.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum FolioAuthType {
  OKAPI,
  NONE // Basically only for dev machines
}
