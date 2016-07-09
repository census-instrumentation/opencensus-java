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
 *     try (CensusScope scope = new CensusScope(TagMap.builder().set("ReadType", "ssd").build())) {
 *       // In scope.
 *       ...
 *     }
 *     // Out of scope.
 *   }
 * }
 * </pre>
 */
public final class CensusScope implements AutoCloseable {
  /**
   * Creates and starts a {@link CensusScope}. Inherits {@link TagMap} from the parent scope
   * and adds specified {@code tags} to it.
   *
   * @param tags additional tags to associate with this scope.
   */
  public CensusScope(TagMap tags) {
    context = CensusContextFactory.getCurrent();
    context.with(tags).setCurrent();
  }

  /**
   * Ends this scope and restores the parent {@link CensusScope}.
   */
  @Override
  public void close() {
    context.setCurrent();
  }

  private final CensusContext context;
}
