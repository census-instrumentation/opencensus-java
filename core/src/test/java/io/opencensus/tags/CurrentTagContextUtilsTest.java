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

import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.tags.TagKey.TagKeyString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CurrentTagContextUtils}. */
@RunWith(JUnit4.class)
public class CurrentTagContextUtilsTest {

  private static final TagContext TAG_CONTEXT =
      TagContext.emptyBuilder()
          .set(TagKeyString.create("Key"), TagValueString.create("Value"))
          .build();

  @Test
  public void testGetCurrentTagContext_WhenNoContext() {
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TagContext.EMPTY);
  }

  @Test
  public void testWithTagContext() {
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TagContext.EMPTY);
    Scope scopedTags = CurrentTagContextUtils.withTagContext(TAG_CONTEXT);
    try {
      assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TAG_CONTEXT);
    } finally {
      scopedTags.close();
    }
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TagContext.EMPTY);
  }

  @Test
  public void testWithTagContextUsingWrap() {
    Runnable runnable;
    Scope scopedTags = CurrentTagContextUtils.withTagContext(TAG_CONTEXT);
    try {
      assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TAG_CONTEXT);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(CurrentTagContextUtils.getCurrentTagContext())
                          .isEqualTo(TAG_CONTEXT);
                    }
                  });
    } finally {
      scopedTags.close();
    }
    assertThat(CurrentTagContextUtils.getCurrentTagContext()).isEqualTo(TagContext.EMPTY);
    // When we run the runnable we will have the TagContext in the current Context.
    runnable.run();
  }
}
