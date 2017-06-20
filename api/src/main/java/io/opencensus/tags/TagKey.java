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

import static com.google.common.base.Preconditions.checkArgument;

import io.opencensus.common.Function;
import io.opencensus.internal.StringUtil;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Tag keys.
 *
 * <p>TagKey's are {@link String}s with enforced restrictions.
 */
@Immutable
public abstract class TagKey {
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;
  protected final String key;

  private TagKey(String key) {
    // TODO(dpo): consider lazy initialization of key.
    // TODO(dpo): consider interning key
    // TODO(dpo): consider whether to sanitize keys or throw an exception.
    checkArgument(StringUtil.isValid(key));
    this.key = StringUtil.sanitize(key);
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Function<StringTagKey, T> p0);

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  // TODO(dpo): consider removing this override and exposing 'key' as package private, for testing.
  @Override
  public final String toString() {
    return key;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof TagKey) && key.equals(((TagKey) obj).key);
  }

  /**
   * String Tag keys.
   *
   * <p>TagKeys that are associated with String values.
   */
  @Immutable
  public static final class StringTagKey extends TagKey {
    private StringTagKey(String key) {
      super(key);
    }

    /**
     * Constructs a new {@link StringTagKey} from the given string. The string will be sanitized
     * such that:
     * <ol>
     * <li>length is restricted to {@link #MAX_LENGTH}, strings longer than that will be truncated.
     * <li>characters are restricted to printable ascii characters, non-printable characters will
     * be replaced by an underscore '_'.
     * </ol>
     */
    // TODO(dpo): consider using a hashtable to create a unique TagKey for a give key and use
    // pointer equality for equals.
    public static StringTagKey create(String key) {
      return new StringTagKey(key);
    }

    @Override
    public <T> T match(
        Function<StringTagKey, T> p0) {
      return p0.apply(this);
    }
  }
}
