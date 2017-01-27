package com.google.instrumentation.common;

import java.io.Closeable;

/**
 * An {@link Closeable} which cannot throw a checked exception.
 *
 * <p>This is useful because such a reversion otherwise requires the caller to catch the
 * (impossible) Exception in the try-with-resources.
 *
 * <p>Example of usage:
 *
 * <pre>
 *   try (NonThrowingAutoCloseable ctx = tryEnter()) {
 *     ...
 *   }
 * </pre>
 */
public interface NonThrowingCloseable extends Closeable {
  @Override
  void close();
}
