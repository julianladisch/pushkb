package com.k_int.pushKb.interactions.folio;

import java.time.Instant;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;

@Serdeable
public class FolioLoginResponseBody {
  @Getter
  Instant accessTokenExpiration;

  @Getter
  Instant refreshTokenExpiration;

  @Getter
  List<FolioErrorBlock> errors;
}
