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

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

/**
 * {@link Census}.
 */
public final class Census {
  private static final CensusContextFactory CONTEXT_FACTORY = new Provider<CensusContextFactory>(
      "com.google.census.CensusContextFactoryImpl").newInstance();

  private static final CensusContext DEFAULT = CONTEXT_FACTORY.getCurrent();

  /** Returns the default {@link CensusContext}. */
  public static CensusContext getDefault() {
    return DEFAULT;
  }

  /** Returns the current thread-local {@link CensusContext}. */
  public static CensusContext getCurrent() {
    return CONTEXT_FACTORY.getCurrent();
  }

  /** Creates a {@link CensusContext} from the given on-the-wire encoded representation. */
  @Nullable
  public static CensusContext deserialize(ByteBuffer buffer) {
    return CONTEXT_FACTORY.deserialize(buffer);
  }
}
