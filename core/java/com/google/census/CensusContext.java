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

import com.google.common.annotations.VisibleForTesting;

import java.nio.ByteBuffer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * An immutable Census-specific context for an operation.
 */
@ThreadSafe
public class CensusContext {
  @VisibleForTesting
  static final CensusContextFactory contextFactory = new Provider<CensusContextFactory>(
      "com.google.census.CensusContextFactoryImpl").newInstance();

  /** The default {@link CensusContext}. */
  public static final CensusContext DEFAULT = new CensusContext(contextFactory.getDefault());

  /** Creates a new {@link CensusContext} built from the given on-the-wire encoded representation.
   *
   * @param buffer on-the-wire representation of a {@link CensusContext}
   * @return a {@link CensusContext} deserialized from {@code buffer}
   */
  public static CensusContext deserialize(ByteBuffer buffer) {
    CensusContextFactory.CensusContext tmpContext = contextFactory.deserialize(buffer);
    return tmpContext == null ? DEFAULT : new CensusContext(tmpContext);
  }

  /**
   * Creates a new {@link CensusContext} by adding the given tags to the tags in this
   * {@link CensusContext}.
   *
   * @param tags the Census key/value pairs to add to {@code this}
   * @return a {@link CensusContext} comprised of the original with the {@code tags} added
   */
  public CensusContext with(TagMap tags) {
    return new CensusContext(context.with(tags));
  }

  /**
   * Records the given metrics against this {@link CensusContext}.
   *
   * @param metrics the metrics to record against the saved {@link CensusContext}
   * @return this
   */
  public CensusContext record(MetricMap metrics) {
    context.record(metrics);
    return this;
  }

  /**
   * Serializes the {@link CensusContext} into the on-the-wire representation.
   *
   * @return serialized bytes.
   */
  public ByteBuffer serialize() {
    return context.serialize();
  }

  final CensusContextFactory.CensusContext context;

  private CensusContext(CensusContextFactory.CensusContext context) {
    this.context = context;
  }

  // Returns the current thread-local CensusContext.
  static CensusContext getCurrent() {
    return new CensusContext(contextFactory.getCurrent());
  }

  // Sets this CensusContext as current in the thread-local context.
  void setCurrent() {
    context.setCurrent();
  }

  // Transfers the current thread's Census stats to this CensusContext.
  void transferCurrentThreadUsage() {
    context.transferCurrentThreadUsage();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof CensusContext) && context.equals(((CensusContext) obj).context);
  }

  @Override
  public int hashCode() {
    return context.hashCode();
  }

  @Override
  public String toString() {
    return context.toString();
  }
}
