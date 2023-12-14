package com.k_int.pushKb;

import java.util.UUID;

import services.k_int.utils.UUIDUtils;

public interface Constants {
	public static interface UUIDs {
		public static final UUID NAMESPACE_PUSHKB = UUIDUtils.nameUUIDFromNamespaceAndString(UUIDUtils.NAMESPACE_DNS, "com.k_int.pushKb");
	}
}
