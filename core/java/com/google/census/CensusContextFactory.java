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

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

interface CensusContextFactory {
  /** Returns the default {@link CensusContext}. */
  CensusContext getDefault();

  /** Returns the current thread-local {@link CensusContext}. */
  CensusContext getCurrent();

  /** Creates a new {@link CensusContext} built from the given on-the-wire encoded representation.
   *
   * @param buffer on-the-wire representation of a {@link CensusContext}
   * @return a {@link CensusContext} deserialized from {@code buffer}
   */
  @Nullable
  CensusContext deserialize(ByteBuffer buffer);

  interface CensusContext {
    /**
     * Creates a new {@link CensusContext} by adding the given tags to the tags in this
     * {@link CensusContext}.
     */
    CensusContext with(TagMap tags);

    /**
     * Serializes the {@link CensusContext} into the on-the-wire representation.
     *
     * @return serialized bytes.
     */
    ByteBuffer serialize();

    /**
     * Records stats against the {@link CensusContext} for the given metrics.
     */
    void record(MetricMap stats);

    /** Sets this {@link CensusContext} as current in the thread-local context. */
    void setCurrent();

    // Tracing API's - these are placeholders at the moment and need more work.

    /**
     * Transfers the current thread's usage, as recorded by {@link record}, to the given
     * {@link CensusContext}.
     */
    void transferCurrentThreadUsage();

    /**
     * Records start of an operation. Caller must call {@link opEnd()} when the operation completes.
     */
    void opStart();

    /**
     * Records end of an operation.
     *
     * @throws IllegalStateException operation has already ended
     */
    void opEnd();

    /**
     * Prints annotations for the {@link CensusContext}.
     *
     * @param format printf-style format string
     * @param args annotations to record
     * @throws IllegalStateException operation has already ended
     */
    void print(String format, Object... args);
  }
}
