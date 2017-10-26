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

package io.opencensus.implcore.tags.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for roundtrip serialization with {@link TagContextBinarySerializerImpl}. */
@RunWith(JUnit4.class)
public class TagContextRoundtripTest {

  private static final TagKey K_EMPTY = TagKey.create("");
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");

  private static final TagValue V_EMPTY = TagValue.create("");
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");

  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final TagContextBinarySerializer serializer =
      tagsComponent.getTagPropagationComponent().getBinarySerializer();
  private final Tagger tagger = tagsComponent.getTagger();

  @Test
  public void testRoundtripSerialization() throws Exception {
    testRoundtripSerialization(tagger.empty());
    testRoundtripSerialization(tagger.emptyBuilder().put(K1, V1).build());
    testRoundtripSerialization(tagger.emptyBuilder().put(K1, V1).put(K2, V2).put(K3, V3).build());
    testRoundtripSerialization(tagger.emptyBuilder().put(K1, V_EMPTY).build());
    testRoundtripSerialization(tagger.emptyBuilder().put(K_EMPTY, V1).build());
    testRoundtripSerialization(tagger.emptyBuilder().put(K_EMPTY, V_EMPTY).build());
  }

  private void testRoundtripSerialization(TagContext expected) throws Exception {
    byte[] bytes = serializer.toByteArray(expected);
    TagContext actual = serializer.fromByteArray(bytes);
    assertThat(actual).isEqualTo(expected);
  }
}
