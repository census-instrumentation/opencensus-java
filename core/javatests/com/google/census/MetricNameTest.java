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
 * Tests for {@link MetricName}
 */
@RunWith(JUnit4.class)
public final class MetricNameTest {
  @Test
  public void testNameMaxLength() {
    char[] name = new char[MetricName.MAX_LENGTH];
    char[] truncName = new char[MetricName.MAX_LENGTH + 10];
    Arrays.fill(name, 'n');
    Arrays.fill(truncName, 'n');
    assertThat(new MetricName(new String(name)).toString())
        .isEqualTo(new MetricName(new String(truncName)).toString());
  }

  @Test
  public void testNameBadChar() {
    assertThat(new MetricName("\2ab\3cd").toString())
        .isEqualTo(Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "ab" + Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    assertThat(new MetricName("foo").equals(new MetricName("foo"))).isTrue();
    assertThat(new MetricName("foo").equals(new MetricName("bar"))).isFalse();
  }

  @Test
  public void testHashCode() {
    assertThat(new MetricName("foo").hashCode()).isEqualTo(new MetricName("foo").hashCode());
    assertThat(new MetricName("foo").hashCode()).isNotEqualTo(new MetricName("bar").hashCode());
  }

  @Test
  public void testToString() {
    assertThat(new MetricName("foo").toString()).isEqualTo("foo");
    assertThat(new MetricName("bar").toString()).isEqualTo("bar");
  }
}
