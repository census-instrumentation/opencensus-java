/*
 * Copyright 2016, Google Inc.
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
