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
import com.k_int.pushKb.services.GoKBFeedService;
import com.k_int.pushKb.services.SourceService;
import com.k_int.pushKb.storage.SourceRecordRepository;

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

  public ComponentSpec<Object> loadSpec(String specName) {
    return ComponentSpec.loadFile(TRANSFORM_SPEC_PATH + "/" + specName);
  }

  public JsonNode convert(ComponentSpec spec, Object inputJson) {
    Context context = Context
      .builder()
      .spec(spec)
      .config(this.config)
      .build();
    Input internal = new Input(inputJson);

    return JsonNode.from(context
      .inputMapper(internal)
      .getComponent()
      .orElse(null));
  }

  public JsonNode convert(ComponentSpec spec, JsonNode inputJson) {
    Object objectJson = null;
    try {
      objectJson = objectMapper.readValue(objectMapper.writeValueAsBytes(inputJson), Object.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return convert(spec, objectJson);
  }

	public Object loadJson(String fileName) throws IOException {
    return objectMapper.readValue(new FileInputStream(TRANSFORM_SPEC_PATH + "/" + fileName), Object.class);
	}



}
