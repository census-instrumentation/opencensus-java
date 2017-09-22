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
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContext}. */
@RunWith(JUnit4.class)
public final class TagContextTest {
  private static final Tag TAG1 =
      TagString.create(TagKeyString.create("key"), TagValueString.create("val"));
  private static final Tag TAG2 =
      TagString.create(TagKeyString.create("key2"), TagValueString.create("val"));

  @Test
  public void equals_IgnoresTagOrderAndTagContextClass() {
    new EqualsTester()
        .addEqualityGroup(
            new SimpleTagContext(TAG1, TAG2),
            new SimpleTagContext(TAG1, TAG2),
            new SimpleTagContext(TAG2, TAG1),
            new TagContext() {
              @Override
              protected Iterator<Tag> iterator() {
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
    private final List<Tag> tags;

    SimpleTagContext(Tag... tags) {
      this(Lists.newArrayList(tags));
    }

    SimpleTagContext(List<Tag> tags) {
      this.tags = tags == null ? null : Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    protected Iterator<Tag> iterator() {
      return tags == null ? null : tags.iterator();
    }
  }
}
