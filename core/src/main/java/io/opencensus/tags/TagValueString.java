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

package io.opencensus.tags;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import io.opencensus.internal.StringUtil;
import io.opencensus.tags.TagKey.TagKeyString;
import javax.annotation.concurrent.Immutable;

/** A validated tag value associated with a {@link TagKeyString}. */
// TODO(sebright): Is there any reason to use a TagValue class for booleans and longs, too?
@Immutable
@AutoValue
public abstract class TagValueString {
  /** The maximum length for a {@code String} tag value. */
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  TagValueString() {}

  /**
   * Constructs a {@code TagValueString} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param value the tag value.
   * @throws IllegalArgumentException if the {@code String} is not valid.
   */
  public static TagValueString create(String value) {
    Preconditions.checkArgument(StringUtil.isValid(value));
    return new AutoValue_TagValueString(value);
  }

  /**
   * Returns the tag value as a {@code String}.
   *
   * @return the tag value as a {@code String}.
   */
  public abstract String asString();
}
