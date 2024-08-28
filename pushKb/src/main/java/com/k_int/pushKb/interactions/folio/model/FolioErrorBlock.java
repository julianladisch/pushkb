package com.k_int.pushKb.interactions.folio.model;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Serdeable
@AllArgsConstructor
@Data
public class FolioErrorBlock {
  @Getter
  String message;
  @Getter
  String type;
  @Getter
  String code;
  @Getter
  List<FolioErrorParameter> parameters;
}
