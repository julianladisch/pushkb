package com.k_int.pushKb.gokb;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.jackson.databind.JacksonDatabindMapper;

import lombok.extern.slf4j.Slf4j;

@Serdeable
@Slf4j
public class GoKBRecord {
  JsonNode jsonRecord;
  private static JacksonDatabindMapper mapper = new JacksonDatabindMapper();

  public GoKBRecord(Object constructRecord) {
    try{
      log.info("TRYING TO MAP: {}", constructRecord);
      this.jsonRecord = mapper.writeValueToTree(constructRecord);
    } catch (Exception e) {
			// Handle any exceptions that might occur during the calculation
			throw new RuntimeException("Error binding to GoKBRecord", e);
		}
  }

  @Override
  public String toString() {
    return jsonRecord.toString();
  }
}
