package com.k_int.pushKb.interactions.folio.model;

import java.time.Instant;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.Getter;

@Serdeable
@Data
public class FolioLoginResponseBody {
  @Getter
  Instant accessTokenExpiration;

  @Getter
  Instant refreshTokenExpiration;

  @Getter
  List<FolioErrorBlock> errors;
}
