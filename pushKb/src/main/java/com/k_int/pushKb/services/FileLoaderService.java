package com.k_int.pushKb.services;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Singleton
public class FileLoaderService {
	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader;

	// Any hardcoded resource paths?
	public static final String TRANSFORM_SPEC_PATH = "classpath:transformSpecs";

	public FileLoaderService(
		ObjectMapper objectMapper,
		ResourceLoader resourceLoader
	) {
		this.objectMapper = objectMapper;
		this.resourceLoader = resourceLoader;
	}

	public InputStream streamFile(final String specName, final String path) {
		Optional<InputStream> resourceStream = resourceLoader.getResourceAsStream(path + "/" + specName);

		return resourceStream.orElseGet(InputStream::nullInputStream);
	}

	public JsonNode readJsonFile(final String specName, final String path) {
		try {
			return objectMapper.readValue(streamFile(specName, path), JsonNode.class);
		}  catch (IOException e) {
			log.error("Error while attempting to read Json File {}, {}", specName, path, e);
			return JsonNode.nullNode();
		}
	}
}
