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

package io.opencensus.implcore.tags;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.tags.TagsTestUtil.tagContextToList;

import com.google.common.collect.ImmutableSet;
import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.unsafe.ContextUtils;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CurrentTagMapUtils}. */
@RunWith(JUnit4.class)
public class CurrentTagMapUtilsTest {
  private static final Tag TAG = Tag.create(TagKey.create("key"), TagValue.create("value"));

  private final TagContext tagContext =
      new TagContext() {

        @Override
        protected Iterator<Tag> getIterator() {
          return ImmutableSet.<Tag>of(TAG).iterator();
        }
      };

  @Test
  public void testGetCurrentTagMap_DefaultContext() {
    TagContext tags = CurrentTagMapUtils.getCurrentTagMap();
    assertThat(tags).isNotNull();
    assertThat(tagContextToList(tags)).isEmpty();
  }

  @Test
  public void testGetCurrentTagMap_ContextSetToNull() {
    Context orig = ContextUtils.withValue(Context.current(), null).attach();
    try {
      TagContext tags = CurrentTagMapUtils.getCurrentTagMap();
      assertThat(tags).isNotNull();
      assertThat(tagContextToList(tags)).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testWithTagMap() {
    assertThat(tagContextToList(CurrentTagMapUtils.getCurrentTagMap())).isEmpty();
    Scope scopedTags = CurrentTagMapUtils.withTagMap(tagContext);
    try {
      assertThat(CurrentTagMapUtils.getCurrentTagMap()).isSameInstanceAs(tagContext);
    } finally {
      scopedTags.close();
    }
    assertThat(tagContextToList(CurrentTagMapUtils.getCurrentTagMap())).isEmpty();
  }

  @Test
  public void testWithTagMapUsingWrap() {
    Runnable runnable;
    Scope scopedTags = CurrentTagMapUtils.withTagMap(tagContext);
    try {
      assertThat(CurrentTagMapUtils.getCurrentTagMap()).isSameInstanceAs(tagContext);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(CurrentTagMapUtils.getCurrentTagMap())
                          .isSameInstanceAs(tagContext);
                    }
                  });
    } finally {
      scopedTags.close();
    }
    assertThat(tagContextToList(CurrentTagMapUtils.getCurrentTagMap())).isEmpty();
    // When we run the runnable we will have the TagContext in the current Context.
    runnable.run();
  }
}
