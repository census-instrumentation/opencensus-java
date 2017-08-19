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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.unsafe.ContextUtils;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CurrentTagContextUtils}. */
@RunWith(JUnit4.class)
public class CurrentTagContextUtilsTest {
  private static final TagString TAG =
      TagString.create(TagKeyString.create("key"), TagValueString.create("value"));

  private final TagContext tagContext =
      new TagContext() {

        @Override
        public Iterator<Tag> unsafeGetIterator() {
          return ImmutableSet.<Tag>of(TAG).iterator();
        }
      };

  @Test
  public void testGetCurrentTagContext_DefaultContext() {
    TagContext tags = CurrentTagContextUtils.getCurrentTagContext();
    assertThat(tags).isNotNull();
    assertThat(asList(tags)).isEmpty();
  }

  @Test
  public void testGetCurrentTagContext_ContextSetToNull() {
    Context orig = Context.current().withValue(ContextUtils.TAG_CONTEXT_KEY, null).attach();
    try {
      TagContext tags = CurrentTagContextUtils.getCurrentTagContext();
      assertThat(tags).isNotNull();
      assertThat(asList(tags)).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testWithTagContext() {
    assertThat(asList(CurrentTagContextUtils.getCurrentTagContext())).isEmpty();
    Scope scopedTags = CurrentTagContextUtils.withTagContext(tagContext);
    try {
      assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(tagContext);
    } finally {
      scopedTags.close();
    }
    assertThat(asList(CurrentTagContextUtils.getCurrentTagContext())).isEmpty();
  }

  @Test
  public void testWithTagContextUsingWrap() {
    Runnable runnable;
    Scope scopedTags = CurrentTagContextUtils.withTagContext(tagContext);
    try {
      assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(tagContext);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(CurrentTagContextUtils.getCurrentTagContext())
                          .isEqualTo(tagContext);
                    }
                  });
    } finally {
      scopedTags.close();
    }
    assertThat(asList(CurrentTagContextUtils.getCurrentTagContext())).isEmpty();
    // When we run the runnable we will have the TagContext in the current Context.
    runnable.run();
  }

  private static List<Tag> asList(TagContext tags) {
    return Lists.newArrayList(tags.unsafeGetIterator());
  }
}
