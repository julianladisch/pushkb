package com.k_int.pushKb.model;

import java.util.UUID;

public interface Source {
  public UUID getId();

  // A source currently must have a URL
  public String getSourceUrl();
}
