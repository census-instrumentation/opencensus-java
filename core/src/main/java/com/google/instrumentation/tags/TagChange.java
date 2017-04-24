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

package com.google.instrumentation.tags;

import com.google.auto.value.AutoValue;
import com.google.instrumentation.internal.StringUtil;
import javax.annotation.concurrent.Immutable;


/** A change to a tag in a {@link TagContext}. */
@Immutable
@AutoValue
public abstract class TagChange {
  public static final int MAX_STRING_LENGTH = StringUtil.MAX_LENGTH;

  TagChange() {}

  public static TagChange create(TagKey<String> key, TagOp op, String value) {
    return new AutoValue_TagChange(key, op, StringUtil.sanitize(value));
  }

  public static TagChange create(TagKey<Long> key, TagOp op, Long value) {
    return new AutoValue_TagChange(key, op, value);
  }

  public static TagChange create(TagKey<Boolean> key, TagOp op, Boolean value) {
    return new AutoValue_TagChange(key, op, value);
  }

  abstract TagKey<?> getKey();

  abstract TagOp getOp();

  abstract Object getValue();
}
