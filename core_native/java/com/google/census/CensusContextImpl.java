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
import java.util.HashMap;

/** Native Implementation of {@link CenusContext} */
final class CensusContextImpl implements CensusContext {
  final HashMap<String, String> tags;

  CensusContextImpl(HashMap<String, String> tags) {
    this.tags = tags;
  }

  @Override
  public Builder builder() {
    return new Builder(tags);
  }

  @Override
  public CensusContext with(TagKey k1, TagValue v1) {
    return builder().set(k1, v1).build();
  }

  @Override
  public CensusContext with(TagKey k1, TagValue v1, TagKey k2, TagValue v2) {
    return builder().set(k1, v1).set(k2, v2).build();
  }

  @Override
  public CensusContext with(
      TagKey k1, TagValue v1, TagKey k2, TagValue v2, TagKey k3, TagValue v3) {
    return builder().set(k1, v1).set(k2, v2).set(k3, v3).build();
  }

  @Override
  public CensusContextImpl record(MetricMap stats) {
    return this;
  }

  @Override
  public ByteBuffer serialize() {
    return CensusSerializer.serialize(this);
  }

  @Override
  public void setCurrent() {
    CensusCurrentContext.set(this);
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

  private static final class Builder implements CensusContext.Builder {
    private final HashMap<String, String> tags;

    private Builder(HashMap<String, String> tags) {
      this.tags = new HashMap<>(tags);
    }

    @Override
    public Builder set(TagKey key, TagValue value) {
      tags.put(key.toString(), value.toString());
      return this;
    }

    @Override
    public CensusContext build() {
      return new CensusContextImpl(new HashMap<>(tags));
    }
  }
}
