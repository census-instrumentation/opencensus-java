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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link CensusScope}
 */
@RunWith(JUnit4.class)
public class CensusScopeTest {
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");
  private static final TagKey K4 = new TagKey("k4");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V3 = new TagValue("v3");
  private static final TagValue V4 = new TagValue("v4");

  @Test
  public void testNoScope() {
    assertEquals(Census.getCurrent(), Census.getDefault());
  }

  @Test
  public void testScope() {
    try (CensusScope scope = new CensusScope(Census.getCurrent().with(K1, V1))) {
      assertEquals(
          Census.getDefault().with(K1, V1),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }

  @Test
  public void testNestedScope() {
    try (CensusScope s1 = new CensusScope(Census.getCurrent().with(K1, V1))) {
      assertEquals(
          Census.getDefault().with(K1, V1),
          Census.getCurrent());
      try (CensusScope s2 = new CensusScope(Census.getCurrent().with(K2, V2))) {
        assertEquals(
            Census.getDefault().with(K1, V1).with(K2, V2),
            Census.getCurrent());
      }
      assertEquals(
          Census.getDefault().with(K1, V1),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }

  @Test
  public void testOf1() {
    try (CensusScope scope = CensusScope.of(K1, V1)) {
      assertEquals(
          Census.getDefault().with(K1, V1),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }

  @Test
  public void testOf2() {
    try (CensusScope scope = CensusScope.of(K1, V1, K2, V2)) {
      assertEquals(
          Census.getDefault().with(K1, V1, K2, V2),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }

  @Test
  public void testOf3() {
    try (CensusScope scope = CensusScope.of(K1, V1, K2, V2, K3, V3)) {
      assertEquals(
          Census.getDefault().with(K1, V1, K2, V2, K3, V3),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }

  @Test
  public void testBuilder() {
    CensusScope.Builder builder =
        CensusScope.builder().set(K1, V1).set(K2, V2).set(K3, V3).set(K4, V4);
    try (CensusScope scope = builder.build()) {
      assertEquals(
          Census.getDefault().builder().set(K1, V1).set(K2, V2).set(K3, V3).set(K4, V4).build(),
          Census.getCurrent());
    }
    assertEquals(Census.getDefault(), Census.getCurrent());
  }
}
