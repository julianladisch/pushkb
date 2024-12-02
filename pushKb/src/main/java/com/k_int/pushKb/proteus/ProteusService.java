package com.k_int.pushKb.proteus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import com.k_int.proteus.ComponentSpec;
import com.k_int.proteus.Config;
import com.k_int.proteus.Context;
import com.k_int.proteus.Input;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.jackson.databind.JacksonDatabindMapper;

@Slf4j
@Singleton
public class ProteusService {
  public static final String TRANSFORM_SPEC_PATH = "classpath:transformSpecs";
  private static final JacksonDatabindMapper jacksonDatabindMapper = new JacksonDatabindMapper();

  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  public ProteusService(
    ObjectMapper objectMapper,
    ResourceLoader resourceLoader
	) {
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
	}

  static final Config config = Config.builder()
                                     .objectMapper(
                                        jacksonDatabindMapper.getObjectMapper()
                                          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                                      )
                                      .pathNotFoundToNull(true)
                                      .build();

  public ComponentSpec<JsonNode> loadSpec(JsonNode jsonNode) {
    try {
      return ComponentSpec.load(JsonNode.class, new ByteArrayInputStream(objectMapper.writeValueAsBytes(jsonNode)));
    } catch (Exception e) {
      log.error("Something went wrong reading json", e);
      return null;
    }
  }
                                    
  public ComponentSpec<JsonNode> loadSpec(String specName) {
    return ComponentSpec.load(JsonNode.class, resourceLoader.getResourceAsStream(TRANSFORM_SPEC_PATH + "/" + specName).get());
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

  public JsonNode convert(ComponentSpec<JsonNode> spec, JsonNode inputJson) throws IOException {
    Context context = getContextFromSpec(spec);
    Input internal = new Input(jacksonDatabindMapper.readValueFromTree(inputJson, com.fasterxml.jackson.databind.JsonNode.class));

    return context
      .inputMapper(internal)
      .asValue(JsonNode.class) // Autoconvert to JsonNode? Doesn't seem to improve performance really
      .orElse(null);
  }
}
