package com.k_int.pushKb.interactions.folio.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

@Serdeable
@Builder
@Data
public class FolioLoginBody {
  private String username;
  private String password;
}
