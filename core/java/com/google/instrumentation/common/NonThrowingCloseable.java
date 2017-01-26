package com.google.instrumentation.common;

import java.io.Closeable;

/**
 * An {@link Closeable} which cannot throw a checked exception.
 */
public interface NonThrowingCloseable extends Closeable {
  @Override
  void close();
}
