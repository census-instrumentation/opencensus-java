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
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link TagKey} to {@link TagValue} that can be used to label anything that is
 * associated with a specific operation.
 *
 * <p>For example, {@code TagContext}s can be used to label stats, log messages, or debugging
 * information.
 */
@Immutable
public abstract class TagContext {

  /**
   * Returns an iterator over the tags in this {@code TagContext}.
   *
   * @return an iterator over the tags in this {@code TagContext}.
   */
  // This method is protected to prevent client code from accessing the tags of any TagContext. We
  // don't currently support efficient access to tags. However, every TagContext subclass needs to
  // provide access to its tags to the stats and tagging implementations by implementing this
  // method. If we decide to support access to tags in the future, we can add a public iterator()
  // method and implement it for all subclasses by calling getIterator().
  //
  // The stats and tagging implementations can access any TagContext's tags through
  // io.opencensus.tags.InternalUtils.getTags, which calls this method.
  protected abstract Iterator<Tag> getIterator();

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
  public boolean equals(@Nullable Object other) {
    if (!(other instanceof TagContext)) {
      return false;
    }
    TagContext otherTags = (TagContext) other;
    Iterator<Tag> iter1 = getIterator();
    Iterator<Tag> iter2 = otherTags.getIterator();
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
    Iterator<Tag> i = getIterator();
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
}
