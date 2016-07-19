/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census;

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
public final class CensusScope implements AutoCloseable {
  private final CensusContext saved;

  /**
   * Saves the current {@link CensusContext} ands installs the given {@link CensusContext} as
   * current. Restores the saved {@link CensusContext} on close.
   */
  public CensusScope(CensusContext context) {
    saved = Census.getCurrent();
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
    private final CensusContext.Builder builder = Census.getCurrent().builder();

    public Builder set(TagKey key, TagValue value) {
      builder.set(key, value);
      return this;
    }

    public CensusScope build() {
      return new CensusScope(builder.build());
    }
  }
}
