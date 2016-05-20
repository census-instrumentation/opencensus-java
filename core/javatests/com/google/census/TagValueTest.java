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

import java.util.Arrays;

/**
 * Tests for {@link TagValue}
 */
@RunWith(JUnit4.class)
public final class TagValueTest {
  @Test
  public void testValueMaxLength() {
    char[] value = new char[TagValue.MAX_LENGTH];
    char[] truncValue = new char[TagValue.MAX_LENGTH + 10];
    Arrays.fill(value, 'v');
    Arrays.fill(truncValue, 'v');
    assertThat(new TagValue(new String(value)).toString())
        .isEqualTo(new TagValue(new String(truncValue)).toString());
  }

  @Test
  public void testValueBadChar() {
    String value = "\2ab\3cd";
    assertThat(new TagValue(value).toString())
        .isEqualTo(Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "ab" + Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    assertThat(new TagValue("foo").equals(new TagValue("foo"))).isTrue();
    assertThat(new TagValue("foo").equals(new TagValue("bar"))).isFalse();
  }

  @Test
  public void testHashCode() {
    assertThat(new TagValue("foo").hashCode()).isEqualTo(new TagValue("foo").hashCode());
    assertThat(new TagValue("foo").hashCode()).isNotEqualTo(new TagValue("bar").hashCode());
  }

  @Test
  public void testToString() {
    assertThat(new TagValue("foo").toString()).isEqualTo("foo");
  }
}
