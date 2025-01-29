package com.k_int.pushKb.crud;

import java.util.UUID;

public interface HasId {
  void setId(UUID id);
  UUID getId();
}
