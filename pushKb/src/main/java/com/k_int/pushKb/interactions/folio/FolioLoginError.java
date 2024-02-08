package com.k_int.pushKb.interactions.folio;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Serdeable
@AllArgsConstructor
@Data
public class FolioLoginError {
  @Getter
  List<FolioErrorBlock> errors;
}
