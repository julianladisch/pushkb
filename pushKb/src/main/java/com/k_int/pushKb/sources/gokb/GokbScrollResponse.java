package com.k_int.pushKb.sources.gokb;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Data
@AllArgsConstructor
@Getter
@Builder
public class GokbScrollResponse {
  String result;
	int scrollSize;
	int lastPage;
	String scrollId;
	boolean hasMoreRecords;
	int size;
	int total;

	@ToString.Exclude // Enable slightly less messy toString of the scroll response for logging
	List<JsonNode> records;
}
