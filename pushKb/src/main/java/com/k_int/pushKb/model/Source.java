package com.k_int.pushKb.model;

import java.util.UUID;

public interface Source {
  // USE UUID5 FOR SOURCE IDs, ENSURE THAT TWO SOURCES CANNOT SHARE AN ID
  public UUID getId();

  // A source currently must have a URL
  public String getSourceUrl();
}
