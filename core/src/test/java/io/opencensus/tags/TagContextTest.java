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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContext}. */
// TODO(sebright): Add more tests once the API is finalized.
@RunWith(JUnit4.class)
public class TagContextTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKeyString KS1 = TagKey.createStringKey("k1");
  private static final TagKeyString KS2 = TagKey.createStringKey("k2");

  private static final TagValueString V1 = TagValueString.create("k1");
  private static final TagValueString V2 = TagValueString.create("k2");

  @Test
  public void applyBuilderOperationsInOrder() {
    assertThat(newBuilder().set(KS1, V1).set(KS1, V2).build().getTags())
        .containsExactly(KS1, V2);
  }

  @Test
  public void allowMutlipleKeysWithSameNameButDifferentTypes() {
    TagKeyString stringKey = TagKey.createStringKey("key");
    TagKeyLong intKey = TagKey.createLongKey("key");
    TagKeyBoolean boolKey = TagKey.createBooleanKey("key");
    assertThat(
            newBuilder()
                .set(stringKey, TagValueString.create("value"))
                .set(intKey, 123)
                .set(boolKey, true)
                .build()
                .getTags())
        .containsExactly(stringKey, TagValueString.create("value"), intKey, 123L, boolKey, true);
  }

  @Test
  public void testSet() {
    TagContext tags = singletonTagContext(KS1, V1);
    assertThat(tags.toBuilder().set(KS1, V2).build().getTags()).containsExactly(KS1, V2);
    assertThat(tags.toBuilder().set(KS2, V2).build().getTags())
        .containsExactly(KS1, V1, KS2, V2);
  }

  @Test
  public void testClear() {
    TagContext tags = singletonTagContext(KS1, V1);
    assertThat(tags.toBuilder().clear(KS1).build().getTags()).isEmpty();
    assertThat(tags.toBuilder().clear(KS2).build().getTags()).containsExactly(KS1, V1);
  }

  private static TagContext.Builder newBuilder() {
    return new TagContext.Builder();
  }

  private static TagContext singletonTagContext(TagKey key, Object value) {
    return new TagContext(ImmutableMap.<TagKey, Object>of(key, value));
  }
}
