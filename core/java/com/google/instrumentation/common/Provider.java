/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.common;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Instrumentation specific service provider mechanism.
 *
 * <pre>{@code
 * // Initialize a static variable using reflection.
 * static final Foo foo = Provider.newInstance("Foo", new NoopFoo());
 * }</pre>
 */
public final class Provider {
  private static final Logger logger = Logger.getLogger(Provider.class.getName());

  /**
   * Returns a new instance of the class specified with {@code name} by invoking the empty-argument
   * constructor via reflections. If the specified class is not found, the {@code defaultValue} is
   * returned.
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <T> T newInstance(String name, @Nullable T defaultValue) {
    try {
      Class<?> provider = Class.forName(name);
      T result = (T) provider.getConstructor().newInstance();
      logger.fine("Loaded: " + name);
      return result;
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Falling back to " + defaultValue, e);
      return defaultValue;
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      } else {
        throw new RuntimeException(e);
      }
    }
  }
}
