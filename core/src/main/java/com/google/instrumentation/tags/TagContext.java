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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;


/** A set of tags. */
@Immutable
@AutoValue
public abstract class TagContext {
  /** The empty {@code TagContext}. */
  private static final TagContext EMPTY = createInternal(new HashMap<TagKey<?>, Object>());

  TagContext() {}

  static TagContext createInternal(Map<TagKey<?>, Object> tags) {
    return new AutoValue_TagContext(new HashMap<TagKey<?>, Object>(tags));
  }

  abstract Map<TagKey<?>, Object> getTags();

  public static TagContext empty() {
    return EMPTY;
  }

  /**
   * Creates a new {@code TagContext} by applying the given {@code TagChange}s.
   */
  public TagContext newContext(TagChange ... changes) {
    Map<TagKey<?>, Object> tags = new HashMap<TagKey<?>, Object>(getTags());
    for (TagChange tc : changes) {
      applyChange(tags, tc);
    }
    return createInternal(tags);
  }

  private static void applyChange(Map<TagKey<?>, Object> tags, TagChange tc) {
    switch (tc.getOp()) {
      case INSERT:
        if (!tags.containsKey(tc.getKey())) {
          tags.put(tc.getKey(), tc.getValue());
        }
        return;
      case SET:
        tags.put(tc.getKey(), tc.getValue());
        return;
      case UPDATE:
        if (tags.containsKey(tc.getKey())) {
          tags.put(tc.getKey(), tc.getValue());
        }
        return;
      case CLEAR:
        tags.remove(tc.getKey());
        return;
      case NOOP:
        return;
    }
  }
}
