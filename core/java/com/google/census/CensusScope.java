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

import java.io.Closeable;

/**
 * {@link CensusScope} defines an arbitrary scope of code as a traceable operation. Supports
 * try-with-resources idiom.
 *
 * <p> Usage:
 * <pre>
 * void handleRead() {
 *   ...
 *   if (ssdRead) {
 *     try (CensusScope scope = CensusScope.builder().set("ReadType", "ssd").build()) {
 *       // In scope.
 *       ...
 *     }
 *     // Out of scope.
 *   }
 * }
 * </pre>
 */
public final class CensusScope implements Closeable {
  private final CensusContext saved;

  /**
   * Saves the current {@link CensusContext} ands installs the given {@link CensusContext} as
   * current. Restores the saved {@link CensusContext} on close.
   */
  public CensusScope(CensusContext context) {
    saved = Census.getCensusContextFactory().getCurrent();
    context.setCurrent();
  }

  /** Ends this scope and restores the saved {@link CensusContext}.  */
  @Override
  public void close() {
    saved.setCurrent();
  }

  /** Returns a {@link Builder} for {@link CensusScope}. */
  public static Builder builder() {
    return new Builder();
  }

  /** Shorthand for builder().set(k1, v1).build() */
  public static CensusScope of(TagKey k1, TagValue v1) {
    return builder().set(k1, v1).build();
  }

  /** Shorthand for builder().set(k1, v1).set(k2, v2).build() */
  public static CensusScope of(TagKey k1, TagValue v1, TagKey k2, TagValue v2) {
    return builder().set(k1, v1).set(k2, v2).build();
  }

  /** Shorthand for builder().set(k1, v1).set(k2, v2).set(k3, v3).build() */
  public static CensusScope of(
      TagKey k1, TagValue v1, TagKey k2, TagValue v2, TagKey k3, TagValue v3) {
    return builder().set(k1, v1).set(k2, v2).set(k3, v3).build();
  }

  /** Builder for {@link CensusScope}. */
  public static final class Builder {
    private final CensusContext.Builder builder =
        Census.getCensusContextFactory().getCurrent().builder();

    public Builder set(TagKey key, TagValue value) {
      builder.set(key, value);
      return this;
    }

    public CensusScope build() {
      return new CensusScope(builder.build());
    }
  }
}
