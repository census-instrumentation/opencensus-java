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

/**
 * Tests for {@link Tag}
 */
@RunWith(JUnit4.class)
public final class TagTest {

  @Test
  public void testGetKey() {
    assertThat(new Tag(KEY, VAL).getKey()).isEqualTo("key");
    assertThat(new Tag("key", "val").getKey()).isEqualTo("key");
  }

  @Test
  public void testGetValue() {
    assertThat(new Tag(KEY, VAL).getValue()).isEqualTo("val");
    assertThat(new Tag("key", "val").getValue()).isEqualTo("val");
  }

  @Test
  public void testSetValue() {
    boolean fail = true;
    try {
      new Tag("key", "val").setValue("val1");
      fail = false;
    } catch (UnsupportedOperationException expected) {
    }
    assertThat(fail).isTrue();
  }

  @Test
  public void testEquals() {
    assertThat(new Tag("key", "val").equals(new Tag(KEY, VAL))).isTrue();
    assertThat(new Tag("key1", "val").equals(new Tag(KEY, VAL))).isFalse();
    assertThat(new Tag("key", "val1").equals(new Tag(KEY, VAL))).isFalse();
    assertThat(new Tag("key1", "val1").equals(new Tag(KEY, VAL))).isFalse();
    assertThat(new Tag("key", "val").equals("foo")).isFalse();
  }

  @Test
  public void testHashCode() {
    assertThat(new Tag("key", "val").hashCode()).isEqualTo(new Tag(KEY, VAL).hashCode());
    assertThat(new Tag("key1", "val").hashCode()).isNotEqualTo(new Tag(KEY, VAL).hashCode());
    assertThat(new Tag("key", "val1").hashCode()).isNotEqualTo(new Tag(KEY, VAL).hashCode());
    assertThat(new Tag("key1", "val1").hashCode()).isNotEqualTo(new Tag(KEY, VAL).hashCode());
  }

  private static final TagKey KEY = new TagKey("key");
  private static final TagValue VAL = new TagValue("val");
}
