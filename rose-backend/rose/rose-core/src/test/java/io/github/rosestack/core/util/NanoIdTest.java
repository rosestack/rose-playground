package io.github.rosestack.core.util;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.rosestack.core.util.NanoId.DEFAULT_ALPHABET;
import static io.github.rosestack.core.util.NanoId.DEFAULT_SIZE;

/**
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since
 */
class NanoIdTest {
	@Test
	void testNanoId() {
		Assertions.assertEquals(DEFAULT_SIZE, NanoId.randomNanoId().length());
		Assertions.assertEquals(10, NanoId.randomNanoId(10).length());
		Assertions.assertEquals(
			10, NanoId.randomNanoId(new Random(), DEFAULT_ALPHABET, 10).length());

		Random random = new Random();
		Assertions.assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(null, DEFAULT_ALPHABET, 10));

		Assertions.assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(random, null, 10));

		Assertions.assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(random, new char[]{}, -1));

		Assertions.assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(random, new char[256], -1));

		Assertions.assertThrows(
			IllegalArgumentException.class, () -> NanoId.randomNanoId(random, DEFAULT_ALPHABET, -1));
	}
}
