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

import com.google.common.collect.Lists;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBinarySerializer;
import io.opencensus.tags.TagContexts;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for roundtrip serialization with {@link TagContextBinarySerializerImpl}. */
@RunWith(JUnit4.class)
public class TagContextRoundtripTest {

  private static final TagKeyString K_EMPTY = TagKeyString.create("");
  private static final TagKeyString K1 = TagKeyString.create("k1");
  private static final TagKeyString K2 = TagKeyString.create("k2");
  private static final TagKeyString K3 = TagKeyString.create("k3");

  private static final TagValueString V_EMPTY = TagValueString.create("");
  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");
  private static final TagValueString V3 = TagValueString.create("v3");

  private final TagContextBinarySerializer serializer = new TagContextBinarySerializerImpl();
  private final TagContexts tagContexts = new TagContextsImpl();

  @Test
  public void testRoundtripSerialization() throws Exception {
    testRoundtripSerialization(tagContexts.empty());
    testRoundtripSerialization(tagContexts.emptyBuilder().set(K1, V1).build());
    testRoundtripSerialization(
        tagContexts.emptyBuilder().set(K1, V1).set(K2, V2).set(K3, V3).build());
    testRoundtripSerialization(tagContexts.emptyBuilder().set(K1, V_EMPTY).build());
    testRoundtripSerialization(tagContexts.emptyBuilder().set(K_EMPTY, V1).build());
    testRoundtripSerialization(tagContexts.emptyBuilder().set(K_EMPTY, V_EMPTY).build());
  }

  private void testRoundtripSerialization(TagContext expected) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    serializer.serialize(expected, output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    TagContext actual = serializer.deserialize(input);
    assertThat(Lists.newArrayList(actual.unsafeGetIterator()))
        .containsExactlyElementsIn(Lists.newArrayList(expected.unsafeGetIterator()));
  }
}
