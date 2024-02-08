package com.k_int.pushKb.interactions.folio;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Serdeable
@AllArgsConstructor
@Data
public class FolioErrorParameter {
  @Getter
  String key;
  @Getter
  String value;
}
