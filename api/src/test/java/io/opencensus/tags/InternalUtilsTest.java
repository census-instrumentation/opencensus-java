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
import io.opencensus.tags.TagContextBuilder.TagScope;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InternalUtils}. */
@RunWith(JUnit4.class)
public final class InternalUtilsTest {

  @Test
  public void getTags() {
    final Iterator<Tag> iterator =
        Lists.<Tag>newArrayList(Tag.create(TagKey.create("k"), TagValue.create("v"))).iterator();
    TagContext ctx =
        new TagContext() {
          @Override
          protected Iterator<Tag> getIterator() {
            return iterator;
          }

          @Override
          public TagScope getTagScope() {
            return TagScope.LOCAL;
          }
        };
    assertThat(InternalUtils.getTags(ctx)).isSameAs(iterator);
  }
}
