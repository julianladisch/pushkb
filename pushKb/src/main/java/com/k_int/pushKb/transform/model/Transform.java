package com.k_int.pushKb.transform.model;


import com.k_int.pushKb.transform.TransformType;
import services.k_int.utils.UUIDUtils;

import java.util.UUID;

import static com.k_int.pushKb.Constants.UUIDs.NAMESPACE_PUSHKB;

// TODO Potentially use Micronaut converters to automatically convert between mapped classes in future
// I'm not sure I fully understand that workflow -- ask Steve

public interface Transform {
	UUID getId();

	String getName();
	String getSlug(); // Unique and CAN NOT CHANGE.
	// TODO Use a DTO on the API to exclude slug field?

	TransformType getType();

	String UUID5_PREFIX = "transform";
	// UUID5 -- name must be unique
	static UUID generateUUID(String slug) {
		final String concat = Transform.UUID5_PREFIX + ":" + slug;
		return UUIDUtils.nameUUIDFromNamespaceAndString(NAMESPACE_PUSHKB, concat);
	}

	static UUID generateUUIDFromTransform(Transform t) {
		return generateUUID(t.getSlug());
	}
}
