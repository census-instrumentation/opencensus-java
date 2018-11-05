/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BigendianEncoding}. */
@RunWith(JUnit4.class)
public class BigendianEncodingTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final long FIRST_LONG = 0x1213141516171819L;
  private static final byte[] FIRST_BYTE_ARRAY =
      new byte[] {0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19};
  private static final long SECOND_LONG = 0xFFEEDDCCBBAA9988L;
  private static final byte[] SECOND_BYTE_ARRAY =
      new byte[] {
        (byte) 0xFF, (byte) 0xEE, (byte) 0xDD, (byte) 0xCC,
        (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88
      };
  private static final byte[] BOTH_BYTE_ARRAY =
      new byte[] {
        0x12,
        0x13,
        0x14,
        0x15,
        0x16,
        0x17,
        0x18,
        0x19,
        (byte) 0xFF,
        (byte) 0xEE,
        (byte) 0xDD,
        (byte) 0xCC,
        (byte) 0xBB,
        (byte) 0xAA,
        (byte) 0x99,
        (byte) 0x88
      };

  @Test
  public void longToByteArray() {
    byte[] result1 = new byte[8];
    BigendianEncoding.longToByteArray(FIRST_LONG, result1, 0);
    assertThat(result1).isEqualTo(FIRST_BYTE_ARRAY);

    byte[] result2 = new byte[8];
    BigendianEncoding.longToByteArray(SECOND_LONG, result2, 0);
    assertThat(result2).isEqualTo(SECOND_BYTE_ARRAY);

    byte[] result3 = new byte[16];
    BigendianEncoding.longToByteArray(FIRST_LONG, result3, 0);
    BigendianEncoding.longToByteArray(SECOND_LONG, result3, 8);
    assertThat(result3).isEqualTo(BOTH_BYTE_ARRAY);
  }

  @Test
  public void longToByteArray_Fails() {
    // These contain bytes not in the decoding.
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("array too small");
    BigendianEncoding.longToByteArray(123, new byte[8], 1);
  }

  @Test
  public void longFromByteArray() {
    assertThat(BigendianEncoding.longFromByteArray(FIRST_BYTE_ARRAY, 0)).isEqualTo(FIRST_LONG);

    assertThat(BigendianEncoding.longFromByteArray(SECOND_BYTE_ARRAY, 0)).isEqualTo(SECOND_LONG);

    assertThat(BigendianEncoding.longFromByteArray(BOTH_BYTE_ARRAY, 0)).isEqualTo(FIRST_LONG);

    assertThat(BigendianEncoding.longFromByteArray(BOTH_BYTE_ARRAY, 8)).isEqualTo(SECOND_LONG);
  }

  @Test
  public void toFromLong() {
    toFromLongValidate(0x8000000000000000L);
    toFromLongValidate(-1);
    toFromLongValidate(0);
    toFromLongValidate(1);
    toFromLongValidate(0x7FFFFFFFFFFFFFFFL);
  }

  @Test
  public void longFromByteArray_Fails() {
    // These contain bytes not in the decoding.
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("array too small");
    BigendianEncoding.longFromByteArray(new byte[8], 1);
  }

  private static void toFromLongValidate(long value) {
    byte[] array = new byte[8];
    BigendianEncoding.longToByteArray(value, array, 0);
    assertThat(BigendianEncoding.longFromByteArray(array, 0)).isEqualTo(value);
  }
}
