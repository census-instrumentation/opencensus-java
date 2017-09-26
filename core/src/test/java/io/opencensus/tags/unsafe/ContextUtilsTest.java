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

package io.opencensus.tags.unsafe;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public final class ContextUtilsTest {
  @Test
  public void testContextKeyName() {
    // Context.Key.toString() returns the name.
    assertThat(ContextUtils.TAG_CONTEXT_KEY.toString()).isEqualTo("opencensus-tag-context-key");
  }

  @Test
  public void testGetCurrentTagContext_DefaultContext() {
    TagContext tags = ContextUtils.TAG_CONTEXT_KEY.get();
    assertThat(tags).isNotNull();
    assertThat(asList(tags)).isEmpty();
  }

  @Test
  public void testGetCurrentTagContext_ContextSetToNull() {
    Context orig = Context.current().withValue(ContextUtils.TAG_CONTEXT_KEY, null).attach();
    try {
      TagContext tags = ContextUtils.TAG_CONTEXT_KEY.get();
      assertThat(tags).isNotNull();
      assertThat(asList(tags)).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  private static List<Tag> asList(TagContext tags) {
    return Lists.newArrayList(InternalUtils.getTags(tags));
  }
}
