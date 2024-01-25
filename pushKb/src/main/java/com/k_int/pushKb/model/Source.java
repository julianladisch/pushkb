package com.k_int.pushKb.model;

import java.util.UUID;

public interface Source {
  public UUID getId();
  public SourceType getSourceType();
  public String getSourceUrl();
}
