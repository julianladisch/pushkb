package com.k_int.pushKb.interactions.folio;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Serdeable
@AllArgsConstructor
@ToString
public class FolioLoginError {
  @Getter
  List<FolioErrorBlock> errors;
}
