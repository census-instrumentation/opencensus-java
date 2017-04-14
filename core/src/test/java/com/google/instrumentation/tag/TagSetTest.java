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

package com.google.instrumentation.tag;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TagSet}.
 */
@RunWith(JUnit4.class)
public class TagSetTest {

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K10 = TagKey.create("k10");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V10 = TagValue.create("v10");

  @Test
  public void testEmpty() {
    assertThat(TagSet.EMPTY.getTags()).isEmpty();
  }

  @Test
  public void testBuilder() {
    assertThat(TagSet.builder().build().getTags()).isEmpty();
    assertThat(TagSet.builder().add(K1, V1).build().getTags())
        .containsExactly(K1, V1);
    assertThat(TagSet.builder().add(K1, V1).add(K2, V2).build().getTags())
        .containsExactly(K1, V1, K2, V2);
  }

  @Test
  public void overrideEarlierKey() {
    assertThat(TagSet.builder().add(K1, V1).add(K1, V2).build().getTags())
        .containsExactly(K1, V2);
  }

  @Test
  public void reuseBuilder() {
    TagSet.Builder builder = TagSet.builder();
    builder.add(K1, V1);
    TagSet tags1 = builder.build();
    builder.add(K1, V2);
    TagSet tags2 = builder.build();
    assertThat(tags1.getTags()).containsExactly(K1, V1);
    assertThat(tags2.getTags()).containsExactly(K1, V2);
  }

  @Test
  public void testWithMethods() {
    assertThat(TagSet.EMPTY.with(K1, V1))
        .isEqualTo(TagSet.builder().add(K1, V1).build());
    assertThat(TagSet.EMPTY.with(K1, V1, K2, V2))
        .isEqualTo(TagSet.builder().add(K1, V1).add(K2, V2).build());
    assertThat(TagSet.EMPTY.with(K1, V1, K2, V2, K3, V3))
        .isEqualTo(TagSet.builder().add(K1, V1).add(K2, V2).add(K3, V3).build());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagSet.EMPTY, TagSet.EMPTY)
        .addEqualityGroup(
            TagSet.EMPTY.with(K1, V1),
            TagSet.EMPTY.with(K1, V1),
            TagSet.EMPTY.with(K1, V1, K1, V1))
        .addEqualityGroup(
            TagSet.EMPTY.with(K1, V1, K2, V2),
            TagSet.EMPTY.with(K1, V1, K2, V2),
            TagSet.EMPTY.with(K2, V2, K1, V1))
        .addEqualityGroup(TagSet.EMPTY.with(K10, V1))
        .addEqualityGroup(TagSet.EMPTY.with(K1, V10))
        .testEquals();
  }
}
