/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Tests for {@link TagMap}
 */
@RunWith(JUnit4.class)
public class TagMapTest {
  @Test
  public void testOf0() {
    assertTagMapEqual(TagMap.of(), 0);
  }

  @Test
  public void testOf1() {
    assertTagMapEqual(TagMap.of(K1, "v1"), 1);
  }

  @Test
  public void testOf2() {
    assertTagMapEqual(TagMap.of(K1, "v1", K2, "v2"), 2);
  }

  @Test
  public void testOf3() {
    assertTagMapEqual(TagMap.of(K1, "v1", K2, "v2", K3, "v3"), 3);
  }

  @Test
  public void testBuilder() {
    TagMap.Builder builder0 = TagMap.builder();
    TagMap.Builder builder1 = TagMap.builder();
    for (int i = 1; i <= 10; i++) {
      builder0.put(new TagKey("k" + i), new TagValue("v" + i));
      assertTagMapEqual(builder0.build(), i);

      builder1.put(new TagKey("k" + i), "v" + i);
      assertTagMapEqual(builder1.build(), i);
    }
  }

  @Test
  public void testDuplicateTagKeys() {
    assertTagMapEqual(TagMap.of(K1, "v1", K1, "v1"), TagMap.of(K1, "v1"));
    assertTagMapEqual(TagMap.of(K1, "v1", K1, "v2"), TagMap.of(K1, "v1"));
    assertTagMapEqual(TagMap.of(K1, "v1", K1, "v2", K1, "v3"), TagMap.of(K1, "v1"));
    assertTagMapEqual(TagMap.of(K1, "v1", K2, "v2", K1, "v3"), TagMap.of(K1, "v1", K2, "v2"));
    assertTagMapEqual(TagMap.of(K1, "v1", K1, "v2", K2, "v2"), TagMap.of(K1, "v1", K2, "v2"));
  }

  @Test
  public void testSize() {
    TagMap.Builder builder = TagMap.builder();
    for (int i = 1; i <= 10; i++) {
      builder.put(new TagKey("k" + i), "v" + i);
      assertThat(builder.build().size()).isEqualTo(i);
    }
  }

  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");

  private static void assertTagMapEqual(TagMap tags1, TagMap tags2) {
    Iterator<Entry<String, String>> tagsIter1 = tags1.iterator();
    Iterator<Entry<String, String>> tagsIter2 = tags2.iterator();
    while (tagsIter1.hasNext() && tagsIter2.hasNext()) {
      assertThat(tagsIter1.next()).isEqualTo(tagsIter2.next());
    }
    assertThat(tagsIter1.hasNext()).isFalse();
    assertThat(tagsIter2.hasNext()).isFalse();
  }

  private static void assertTagMapEqual(TagMap tags, int len) {
    TagMap.Builder builder = TagMap.builder();
    for (int i = 1; i <= len; i++) {
      builder.put(new TagKey("k" + i), "v" + i);
    }
    assertTagMapEqual(tags, builder.build());
  }
}
