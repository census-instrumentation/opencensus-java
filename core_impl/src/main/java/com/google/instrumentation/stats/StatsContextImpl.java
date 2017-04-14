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

package com.google.instrumentation.stats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Native Implementation of {@link StatsContext}.
 */
final class StatsContextImpl extends StatsContext {

  final HashMap<String, String> tags;

  StatsContextImpl(HashMap<String, String> tags) {
    this.tags = tags;
  }

  @Override
  public Builder builder() {
    return new Builder(tags);
  }

  @Override
  public StatsContextImpl record(MeasurementMap stats) {
    return this;
  }

  /**
   * Serializes a {@link StatsContextImpl} into on-the-wire serialized format.
   *
   * <p>The encoded tags are of the form: {@code <version_id><encoded_tags>}
   *
   * @return a byte array that represents the serialized StatsContext.
   */
  @Override
  public byte[] serialize() throws IOException {
    return StatsSerializer.serialize(this);
  }

  @Deprecated
  @Override
  public void serialize(OutputStream output) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(this.serialize());
    byteArrayOutputStream.writeTo(output);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof StatsContextImpl) && tags.equals(((StatsContextImpl) obj).tags);
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  @Override
  public String toString() {
    return tags.toString();
  }

  private static final class Builder extends StatsContext.Builder {

    private final HashMap<String, String> tags;

    private Builder(HashMap<String, String> tags) {
      this.tags = new HashMap<String, String>(tags);
    }

    @Override
    public Builder set(TagKey key, TagValue value) {
      tags.put(key.toString(), value.toString());
      return this;
    }

    @Override
    public StatsContext build() {
      return new StatsContextImpl(new HashMap<String, String>(tags));
    }
  }
}
