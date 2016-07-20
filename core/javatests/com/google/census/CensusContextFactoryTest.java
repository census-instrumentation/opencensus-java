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

import static com.google.common.truth.Truth.assertThat;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;

/**
 * Tests for {@link CensusContextFactory}.
 */
@RunWith(JUnit4.class)
public class CensusContextFactoryTest {
  @Test
  public void testDeserializeEmpty() {
    assertThat(Census.deserialize(ByteBuffer.wrap(new byte[0]))).isEqualTo(Census.getDefault());
  }

  @Test
  public void testDeserializeBadData() {
    assertThat(Census.deserialize(ByteBuffer.wrap("\2as\3df\2".getBytes(UTF_8))))
        .isNull();
  }

  @Test
  public void testGetCurrent() {
    assertThat(Census.getCurrent()).isEqualTo(Census.getDefault());
  }
}
