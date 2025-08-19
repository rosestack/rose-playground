package io.github.rosestack.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ExceptionUtils {
	public static <E extends Throwable> void uncheckedThrow(Throwable t) throws E {
		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		}

		if (t instanceof IOException) {
			throw new UncheckedIOException((IOException) t);
		}

		if (t instanceof InterruptedException) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(t);
		}

		throw new RuntimeException(t);
	}
}
