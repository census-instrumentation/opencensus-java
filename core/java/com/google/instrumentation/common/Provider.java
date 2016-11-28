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

/**
 * Instrumentation specific service provider mechanism.
 *
 * <pre>{@code
 * // Initialize a static variable using reflection.
 * static final Foo foo = Provider<Foo>.newInstance("Foo", new NoopFoo());
 * }</pre>
 */
public final class Provider {
  /**
   * Returns a new instance of the class specified with {@code name} by invoking the empty-argument
   * constructor via reflections. If there are any errors, the {@code defaultValue} is returned.
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(String name, T defaultValue) {
    try {
      Class<?> provider = Class.forName(name);
      return (T) provider.getConstructor().newInstance();
    } catch (ClassNotFoundException e) {
    } catch (IllegalAccessException e) {
    } catch (InstantiationException e) {
    } catch (InvocationTargetException e) {
    } catch (NoSuchMethodException e) {
    }
    // No implementation available. Return the default value.
    return defaultValue;
  }
}
