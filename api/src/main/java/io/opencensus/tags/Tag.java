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

package io.opencensus.tags;

import io.opencensus.common.Function;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A pair of consisting of an associated {@link TagKey} and {@link TagValue}.
 */
@Immutable
public abstract class Tag<TagKeyT extends TagKey, TagValueT> {
  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Function<StringTag, T> p0);

  /**
   * Returns the associated tag key.
   */
  public abstract TagKeyT getKey();

  public abstract TagValueT getValue();

  protected final TagKeyT key;
  protected final TagValueT value;

  private Tag(TagKeyT key, TagValueT value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Tag
        && key.equals(((Tag) obj).key)
        && value.equals(((Tag) obj).value);
  }

  @Override
  public int hashCode() {
    return 31 * key.hashCode() + value.hashCode();
  }

  @Override
  public String toString() {
    return "<" + key + "," + value + ">";
  }


  /**
   * A pair of consisting of an associated {@link StringTagKey} and {@link String}.
   */
  public static final class StringTag extends Tag<TagKey.StringTagKey, String> {

    private StringTag(TagKey.StringTagKey key, String value) {
      super(key, value);
    }

    /**
     * Constructs a new {@link StringTag} from the given key and value.
     */
    public static StringTag create(TagKey.StringTagKey key, String value) {
      return new StringTag(key, value);
    }

    @Override
    public TagKey.StringTagKey getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public <T> T match(
        Function<StringTag, T> p0) {
      return p0.apply(this);
    }
  }
}
