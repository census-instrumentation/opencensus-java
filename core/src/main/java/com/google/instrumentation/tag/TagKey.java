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
import com.google.instrumentation.internal.StringUtil;
import javax.annotation.concurrent.Immutable;


/**
 * Tag keys.
 *
 * <p>{@code TagKey}s are {@code String}s with enforced restrictions.
 */
@Immutable
@AutoValue
public abstract class TagKey {
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  TagKey() {}

  /**
   * Constructs a {@link TagKey} from the given string. The string will be sanitized such that:
   *
   * <ol>
   *   <li>length is restricted to {@link StringUtil#MAX_LENGTH}, strings longer than that will be
   *       truncated.
   *   <li>characters are restricted to printable ascii characters, non-printable characters will be
   *       replaced by an underscore '_'.
   * </ol>
   */
  public static TagKey create(String key) {
    return new AutoValue_TagKey(StringUtil.sanitize(key));
  }

  public abstract String asString();
}
