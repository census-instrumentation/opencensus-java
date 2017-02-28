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

import java.util.ServiceConfigurationError;
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

  /**
   * Tries to create an instance of the given rawClass as a subclass of the given superclass.
   *
   * @param rawClass The class that is initialized.
   * @param superclass The initialized class must be a subclass of this.
   * @return an instance of the class given rawClass which is a subclass of the given superclass.
   * @throws ServiceConfigurationError if any error happens.
   */
  public static <T> T createInstance(Class<?> rawClass, Class<T> superclass) {
    try {
      return rawClass.asSubclass(superclass).getConstructor().newInstance();
    } catch (Exception e) {
      throw new ServiceConfigurationError(
          "Provider " + rawClass.getName() + " could not be instantiated.", e);
    }
  }

  /**
   * Get the correct {@link ClassLoader} that must be used when loading using reflection.
   *
   * @return The correct {@code ClassLoader} that must be used when loading using reflection.
   */
  public static <T> ClassLoader getCorrectClassLoader(Class<T> superClass) {
    if (isAndroid()) {
      // When android:sharedUserId or android:process is used, Android will setup a dummy
      // ClassLoader for the thread context (http://stackoverflow.com/questions/13407006),
      // instead of letting users to manually set context class loader, we choose the
      // correct class loader here.
      return superClass.getClassLoader();
    }
    return Thread.currentThread().getContextClassLoader();
  }

  private static boolean isAndroid() {
    try {
      Class.forName("android.app.Application", /*initialize=*/ false, null);
      return true;
    } catch (Exception e) {
      // If Application isn't loaded, it might as well not be Android.
      return false;
    }
  }
}
