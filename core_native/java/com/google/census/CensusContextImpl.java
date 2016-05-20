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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

/** Native Implementation of {@link CensusContextFactory.CenusContext} */
final class CensusContextImpl implements CensusContextFactory.CensusContext {
  static final char TAG_PREFIX = '\2';
  static final char TAG_DELIM = '\3';

  CensusContextImpl(HashMap<String, String> tags) {
    this.tags = tags;
  }

  private final HashMap<String, String> tags;

  @Override
  public CensusContextImpl with(TagMap tags) {
    HashMap<String, String> newTags = new HashMap(this.tags.size() + tags.size());
    newTags.putAll(this.tags);
    for (Entry<String, String> tag : tags) {
      newTags.put(tag.getKey(), tag.getValue());
    }
    return new CensusContextImpl(newTags);
  }

  @Override
  public ByteBuffer serialize() {
    // Note: for now we serialize into the Google3 Census on-the-wire format. Eventually
    // we will standardize on format (likely protobuf based).
    //
    // TODO(dpo): update encoding once format has been finalized.
    StringBuilder builder = new StringBuilder();
    for (Entry<String, String> tag : tags.entrySet()) {
      builder
          .append(TAG_PREFIX)
          .append(tag.getKey())
          .append(TAG_DELIM)
          .append(tag.getValue());
    }
    return ByteBuffer.wrap(builder.toString().getBytes(UTF_8));
  }

  @Override
  public void record(MetricMap stats) {
  }

  @Override
  public void setCurrent() {
    CensusContextFactoryImpl.contexts.set(this);
  }

  @Override
  public void transferCurrentThreadUsage() {
  }

  @Override
  public void opStart() {
  }

  @Override
  public void opEnd() {
  }

  @Override
  public void print(String format, Object... args) {
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof CensusContextImpl) && tags.equals(((CensusContextImpl) obj).tags);
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  @Override
  public String toString() {
    return tags.toString();
  }
}
