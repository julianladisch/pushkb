package com.k_int.pushKb.gokb;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Data
@AllArgsConstructor
public class GokbScrollResponse {
  String result;
	int scrollSize;
	int lastPage;
	String scrollId;
	boolean hasMoreRecords;
	int size;
	int total;

	@ToString.Exclude
	List<JsonNode> records;
}
