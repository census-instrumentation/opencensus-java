/*
 * Copyright 2017, Google Inc.
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

package com.google.instrumentation.tag;

import com.google.auto.value.AutoValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;


/** A set of tags. */
@Immutable
@AutoValue
public abstract class TagSet {
  /** The empty {@code TagSet}. */
  public static final TagSet EMPTY = builder().build();

  TagSet() {}

  static TagSet create(HashMap<TagKey, TagValue> tags) {
    return new AutoValue_TagSet(tags);
  }

  abstract Map<TagKey, TagValue> getTags();

  /** Returns an empty builder. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a builder with the same contents as the given {@code TagSet}.
   *
   * @param initialTags the initial tags.
   * @return a builder containing the initial tags.
   */
  public static Builder builder(TagSet initialTags) {
    return new Builder(initialTags.getTags());
  }

  /** Shorthand for {@code builder(tagSet).set(k1, v1).build()}. */
  public final TagSet with(TagKey k1, TagValue v1) {
    return builder(this).add(k1, v1).build();
  }

  /** Shorthand for {@code builder(tagSet).set(k1, v1).set(k2, v2).build()}. */
  public final TagSet with(TagKey k1, TagValue v1, TagKey k2, TagValue v2) {
    return builder(this).add(k1, v1).add(k2, v2).build();
  }

  /** Shorthand for {@code builder(tagSet).set(k1, v1).set(k2, v2).set(k3, v3).build()}. */
  public final TagSet with(TagKey k1, TagValue v1, TagKey k2, TagValue v2, TagKey k3, TagValue v3) {
    return builder(this).add(k1, v1).add(k2, v2).add(k3, v3).build();
  }

  /** Builder for {@link TagSet}. */
  public static final class Builder {
    private final Map<TagKey, TagValue> tags;

    private Builder() {
      this.tags = new HashMap<TagKey, TagValue>();
    }

    private Builder(Map<TagKey, TagValue> tags) {
      this.tags = new HashMap<TagKey, TagValue>(tags);
    }

    /**
     * Associates the given tag key with the given tag value.
     *
     * @param key the key
     * @param value the value to be associated with {@code key}
     * @return {@code this}
     */
    public Builder add(TagKey key, TagValue value) {
      tags.put(key, value);
      return this;
    }

    /** Builds a {@link TagSet} from the specified keys and values. */
    public TagSet build() {
      return TagSet.create(new HashMap<TagKey, TagValue>(tags));
    }
  }
}
