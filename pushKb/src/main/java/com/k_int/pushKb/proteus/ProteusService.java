package com.k_int.pushKb.proteus;

import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import com.k_int.proteus.ComponentSpec;
import com.k_int.proteus.Config;
import com.k_int.proteus.Context;
import com.k_int.proteus.Input;

@Slf4j
@Singleton
public class ProteusService {
  public static final String TRANSFORM_SPEC_PATH = "src/main/resources/transformSpecs";
  private final ObjectMapper objectMapper;

  public ProteusService(
    ObjectMapper objectMapper
	) {
		this.objectMapper = objectMapper;
	}

  static final Config config = Config.builder()
                                     .objectMapper(
                                        new com.fasterxml.jackson.databind.ObjectMapper()
                                          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                                      )
                                      .pathNotFoundToNull(true)
                                      .build();

  public ComponentSpec<JsonNode> loadSpec(String specName) {
    return ComponentSpec.loadFile(JsonNode.class, TRANSFORM_SPEC_PATH + "/" + specName);
  }

  public Context getContextFromSpec(String specName) {
    ComponentSpec<JsonNode> spec = loadSpec(specName);
    return getContextFromSpec(spec);
  }

  public Context getContextFromSpec(ComponentSpec<JsonNode> spec) {
    return Context
      .builder()
      .spec(spec)
      .config(ProteusService.config)
      .build();
  }

  public Object objectFromJsonNode(JsonNode json) {
    Object objectJson = null;
    try {
      objectJson = objectMapper.readValue(objectMapper.writeValueAsBytes(json), Object.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return objectJson;
  }

  public JsonNode convert(Context context, JsonNode inputJson) {
    Input internal = new Input(inputJson.toString());

    return JsonNode.from(context
      .inputMapper(internal)
      .getComponent()
      .orElse(null));
  }

  public JsonNode convert(ComponentSpec<JsonNode> spec, JsonNode inputJson) {
    Context context = getContextFromSpec(spec);
    return convert(context, inputJson);
  }
}
