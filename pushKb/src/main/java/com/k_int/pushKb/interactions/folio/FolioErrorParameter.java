package com.k_int.pushKb.interactions.folio;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Serdeable
@AllArgsConstructor
@ToString
public class FolioErrorParameter {
  @Getter
  String key;
  @Getter
  String value;
}
