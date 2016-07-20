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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Provider}
 */
@RunWith(JUnit4.class)
public class ProviderTest {
  @Test
  public void testGoodClass() throws Exception {
    Provider<GetGen> provider = new Provider<>("com.google.census.ProviderTest$GetGen");
    GetGen getGen0 = provider.newInstance();
    assertThat(getGen0.getGen()).isEqualTo(0);
    for (int i = 1; i < 10; i++) {
      assertThat(provider.newInstance().getGen()).isEqualTo(i);
    }
    assertThat(getGen0.getGen()).isEqualTo(0);
  }

  @Test
  public void testBadClass() throws Exception {
    Provider<GetGen> provider = new Provider<>("com.google.census.ProviderTest$BadClass");
    assertThat(provider).isNotNull();
    assertThat(provider.newInstance()).isNull();
  }

  static class GetGen {
    static int genCount = 0;
    int gen;

    public GetGen() {
      gen = genCount++;
    }

    public int getGen() {
      return gen;
    }
  }
}
