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
import io.grpc.Context;
import io.opencensus.common.Scope;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CurrentTagContextUtils}. */
@RunWith(JUnit4.class)
public class CurrentTagContextUtilsTest {
  private final TagContext tagContext =
      new TagContext() {

        @Override
        public Iterator<Tag> unsafeGetIterator() {
          return ImmutableSet.<Tag>of().iterator();
        }
      };

  @Test
  public void testGetCurrentTagContext_WhenNoContext() {
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isNull();
  }

  @Test
  public void testWithTagContext() {
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isNull();
    Scope scopedTags = CurrentTagContextUtils.withTagContext(tagContext);
    try {
      assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(tagContext);
    } finally {
      scopedTags.close();
    }
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isNull();
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
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isNull();
    // When we run the runnable we will have the TagContext in the current Context.
    runnable.run();
  }
}
