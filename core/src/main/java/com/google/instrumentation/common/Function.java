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

/**
 * Used to specify matching functions for use encoding tagged unions (i.e. sum types) in Java. See
 * {@link com.google.instrumentation.stats.ViewDescriptor} for an example of it's use.
 *
 * <p>Note: This class is based on the java.util.Function class added in Java 1.8.  We cannot use
 * the Function from Java 1.8 because this library is Java 1.6 compatible.
 */
public interface Function<A, B> {
  B apply(A arg);
}
