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

package com.google.census;

import java.lang.reflect.InvocationTargetException;

/**
 * Census-specific service provider mechanism.
 *
 * <pre>{@code
 * // Define a Provider during static initialization.
 * static final Provider<FooProvider> fooProvider = new Provider("FooProvider");
 *
 * // Use the Provider during execution to generate new instances of the provided class.
 * FooProvider providerInstance = fooProvider.newInstance();
 * }</pre>
 */
class Provider<T> {
  Class<?> provider;

  Provider(String name) {
    try {
      provider = Class.forName(name);
    } catch (ClassNotFoundException expected) {
      // TODO(dpo): decide how to handle when provider classes are not available.
      provider = null;
    }
  }

  @SuppressWarnings("unchecked")
  T newInstance() {
    try {
      if (provider != null) {
        return (T) provider.getConstructor().newInstance();
      }
    } catch (InstantiationException e) {
      // TODO(dpo): decide what to do here - log, crash, or both.
      System.err.println(e);
    } catch (IllegalAccessException e) {
      // TODO(dpo): decide what to do here - log, crash, or both.
      System.err.println(e);
    } catch (NoSuchMethodException e) {
      // TODO(dpo): decide what to do here - log, crash, or both.
      System.err.println(e);
    } catch (InvocationTargetException e) {
      // TODO(dpo): decide what to do here - log, crash, or both.
      System.err.println(e);
    }
    return null;
  }
}
