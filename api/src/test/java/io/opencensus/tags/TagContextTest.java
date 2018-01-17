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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContext}. */
@RunWith(JUnit4.class)
public final class TagContextTest {
  private static final Tag TAG1 = Tag.create(TagKey.create("key"), TagValue.create("val"));
  private static final Tag TAG2 = Tag.create(TagKey.create("key2"), TagValue.create("val"));

  @Test
  public void equals_IgnoresTagOrderAndTagContextClass() {
    new EqualsTester()
        .addEqualityGroup(
            new SimpleTagContext(TAG1, TAG2),
            new SimpleTagContext(TAG1, TAG2),
            new SimpleTagContext(TAG2, TAG1),
            new TagContext() {
              @Override
              protected Iterator<Tag> getIterator() {
                return Lists.newArrayList(TAG1, TAG2).iterator();
              }
            })
        .testEquals();
  }

  @Test
  public void equals_HandlesNullIterator() {
    new EqualsTester()
        .addEqualityGroup(
            new SimpleTagContext((List<Tag>) null),
            new SimpleTagContext((List<Tag>) null),
            new SimpleTagContext())
        .testEquals();
  }

  @Test
  public void equals_DoesNotIgnoreNullTags() {
    new EqualsTester()
        .addEqualityGroup(new SimpleTagContext(TAG1))
        .addEqualityGroup(new SimpleTagContext(TAG1, null), new SimpleTagContext(null, TAG1))
        .addEqualityGroup(new SimpleTagContext(TAG1, null, null))
        .testEquals();
  }

  @Test
  public void equals_DoesNotIgnoreDuplicateTags() {
    new EqualsTester()
        .addEqualityGroup(new SimpleTagContext(TAG1))
        .addEqualityGroup(new SimpleTagContext(TAG1, TAG1))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(new SimpleTagContext().toString()).isEqualTo("TagContext");
    assertThat(new SimpleTagContext(TAG1, TAG2).toString()).isEqualTo("TagContext");
  }

  private static final class SimpleTagContext extends TagContext {
    @Nullable private final List<Tag> tags;

    // This Error Prone warning doesn't seem correct, because the constructor is just calling
    // another constructor.
    @SuppressWarnings("ConstructorLeaksThis")
    SimpleTagContext(Tag... tags) {
      this(Lists.newArrayList(tags));
    }

    SimpleTagContext(List<Tag> tags) {
      this.tags = tags == null ? null : Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    @Nullable
    protected Iterator<Tag> getIterator() {
      return tags == null ? null : tags.iterator();
    }
  }
}
