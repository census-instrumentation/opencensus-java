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
import javax.annotation.concurrent.ThreadSafe;

/**
 * An immutable Census-specific context for an operation.
 */
@ThreadSafe
public abstract class CensusContext {
  /**
   * Creates a new {@link CensusContext} by adding the given tags to the tags in this
   * {@link CensusContext}.
   *
   * @param tags the Census key/value pairs to add to {@code this}
   * @return a {@link CensusContext} comprised of the original with the {@code tags} added
   */
  public abstract CensusContext with(TagMap tags);

  /**
   * Records the given metrics against this {@link CensusContext}.
   *
   * @param metrics the metrics to record against the saved {@link CensusContext}
   * @return this
   */
  public abstract CensusContext record(MetricMap metrics);

  /**
   * Serializes the {@link CensusContext} into the on-the-wire representation.
   *
   * @return serialized bytes.
   */
  public abstract ByteBuffer serialize();

  // Note: These method should be static. They are accessed via CensusContextFactory.
  @Nullable
  abstract CensusContext deserialize(ByteBuffer buffer);

  abstract CensusContext getCurrent();

  // TODO(dpo): condsider moving these methods to another class.
  // Sets this CensusContext as current in the thread-local context.
  abstract void setCurrent();

  // Transfers the current thread's Census stats to this CensusContext.
  abstract void transferCurrentThreadUsage();
}
