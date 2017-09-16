/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.tags;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;

/**
 * A map from keys to values that can be used to label anything that is associated with a specific
 * operation.
 *
 * <p>For example, {@code TagContext}s can be used to label stats, log messages, or debugging
 * information.
 *
 * <p>Keys have type {@link TagKey}. Values have type {@link TagValueString}, though the library
 * will support more types in the future, including {@code long} and {@code boolean}.
 */
@Immutable
public abstract class TagContext {
  private static final TagContext NOOP_TAG_CONTEXT = new NoopTagContext();

  // TODO(sebright): Consider removing TagContext.unsafeGetIterator() so that we don't need to
  // support fast access to tags.
  public abstract Iterator<Tag> unsafeGetIterator();

  @Override
  public String toString() {
    return "TagContext";
  }

  /**
   * Returns true iff the other object is an instance of {@code TagContext} and contains the same
   * key-value pairs. Implementations are free to override this method to provide better
   * performance.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TagContext)) {
      return false;
    }
    TagContext otherTags = (TagContext) other;
    Iterator<Tag> iter1 = unsafeGetIterator();
    Iterator<Tag> iter2 = otherTags.unsafeGetIterator();
    Multiset<Tag> tags1 =
        iter1 == null
            ? ImmutableMultiset.<Tag>of()
            : HashMultiset.create(Lists.newArrayList(iter1));
    Multiset<Tag> tags2 =
        iter2 == null
            ? ImmutableMultiset.<Tag>of()
            : HashMultiset.create(Lists.newArrayList(iter2));
    return tags1.equals(tags2);
  }

  @Override
  public final int hashCode() {
    int hashCode = 0;
    Iterator<Tag> i = unsafeGetIterator();
    if (i == null) {
      return hashCode;
    }
    while (i.hasNext()) {
      Tag tag = i.next();
      if (tag != null) {
        hashCode += tag.hashCode();
      }
    }
    return hashCode;
  }

  /**
   * Returns a {@code TagContext} that does not contain any tags.
   *
   * @return a {@code TagContext} that does not contain any tags.
   */
  static TagContext getNoopTagContext() {
    return NOOP_TAG_CONTEXT;
  }

  @Immutable
  private static final class NoopTagContext extends TagContext {

    // TODO(sebright): Is there any way to let the user know that their tags were ignored?
    @Override
    public Iterator<Tag> unsafeGetIterator() {
      return Collections.<Tag>emptySet().iterator();
    }
  }
}
