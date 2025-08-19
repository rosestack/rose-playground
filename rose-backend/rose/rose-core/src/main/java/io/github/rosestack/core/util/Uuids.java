package io.github.rosestack.core.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public abstract class Uuids {
	// Use Java 8's built-in Base64 encoder/decoder
	private static final Base64.Encoder URL_SAFE_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_SAFE_DECODER = Base64.getUrlDecoder();

	private Uuids() {
	}

	public static UUID getUUID() {
		return UuidCreator.getTimeOrderedEpoch(); // UUIDv7
	}

	/**
	 * Generate a UUID and encode it to a URL-safe Base64 string.
	 *
	 * @return A URL-safe Base64 encoded UUID string.
	 */
	public static String uuidToBase64(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return URL_SAFE_ENCODER.encodeToString(bb.array());
	}

	/**
	 * Decode a URL-safe Base64 string back to a UUID.
	 *
	 * @param base64 A URL-safe Base64 encoded UUID string.
	 * @return The decoded UUID.
	 */
	public static UUID base64ToUuid(String base64) {
		byte[] bytes = URL_SAFE_DECODER.decode(base64);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long high = bb.getLong();
		long low = bb.getLong();
		return new UUID(high, low);
	}
}
