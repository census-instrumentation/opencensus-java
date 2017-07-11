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

import com.google.common.collect.ImmutableMap;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContext}. */
// TODO(sebright): Add more tests once the API is finalized.
@RunWith(JUnit4.class)
public class TagContextTest {

  private static final TagKeyString KS1 = TagKeyString.create("k1");
  private static final TagKeyString KS2 = TagKeyString.create("k2");

  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");

  @Test
  public void testEmpty() {
    assertThat(TagContext.EMPTY.getTags()).isEmpty();
  }

  @Test
  public void applyBuilderOperationsInOrder() {
    assertThat(TagContext.newBuilder().set(KS1, V1).set(KS1, V2).build().getTags())
        .containsExactly(KS1, V2);
  }

  @Test
  public void allowMutlipleKeysWithSameNameButDifferentTypes() {
    TagKeyString stringKey = TagKeyString.create("key");
    TagKeyLong longKey = TagKeyLong.create("key");
    TagKeyBoolean boolKey = TagKeyBoolean.create("key");
    assertThat(
            TagContext.newBuilder()
                .set(stringKey, TagValueString.create("value"))
                .set(longKey, 123)
                .set(boolKey, true)
                .build()
                .getTags())
        .containsExactly(stringKey, TagValueString.create("value"), longKey, 123L, boolKey, true);
  }

  @Test
  public void testSet() {
    TagContext tags = singletonTagContext(KS1, V1);
    assertThat(tags.toBuilder().set(KS1, V2).build().getTags()).containsExactly(KS1, V2);
    assertThat(tags.toBuilder().set(KS2, V2).build().getTags()).containsExactly(KS1, V1, KS2, V2);
  }

  @Test
  public void testClear() {
    TagContext tags = singletonTagContext(KS1, V1);
    assertThat(tags.toBuilder().clear(KS1).build().getTags()).isEmpty();
    assertThat(tags.toBuilder().clear(KS2).build().getTags()).containsExactly(KS1, V1);
  }

  private static TagContext singletonTagContext(TagKey key, Object value) {
    return new TagContext(ImmutableMap.<TagKey, Object>of(key, value));
  }
}
