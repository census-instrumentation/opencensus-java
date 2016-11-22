package com.google.instrumentation.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Timestamp}. */
@RunWith(JUnit4.class)
public class TimestampTest {
  @Test
  public void testTimestampCreate() {
    assertThat(Timestamp.create(24, 42).getSeconds()).isEqualTo(24);
    assertThat(Timestamp.create(24, 42).getNanos()).isEqualTo(42);
    assertThat(Timestamp.create(-24, 42).getSeconds()).isEqualTo(-24);
    assertThat(Timestamp.create(-24, 42).getNanos()).isEqualTo(42);
  }

  @Test
  public void testTimestampCreateInvalidInput() {
    assertThat(Timestamp.create(-315576000001L, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(315576000001L, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(1, 1000000000)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(1, -1)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(-1, 1000000000)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(-1, -1)).isEqualTo(Timestamp.create(0, 0));
  }

  @Test
  public void testTimestampFromMillis() {
    assertThat(Timestamp.fromMillis(0)).isEqualTo(Timestamp.create(0,0));
    assertThat(Timestamp.fromMillis(987)).isEqualTo(Timestamp.create(0, 987000000));
    assertThat(Timestamp.fromMillis(3456)).isEqualTo(Timestamp.create(3, 456000000));
  }

  @Test
  public void testTimestampFromMillisNegative() {
    assertThat(Timestamp.fromMillis(-1)).isEqualTo(Timestamp.create(-1, 999000000));
    assertThat(Timestamp.fromMillis(-999)).isEqualTo(Timestamp.create(-1, 1000000));
    assertThat(Timestamp.fromMillis(-3456)).isEqualTo(Timestamp.create(-4, 544000000));
  }

  @Test
  public void testTimestampEqual() {
    // Positive tests.
    assertThat(Timestamp.create(0, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(24, 42)).isEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-24, 42)).isEqualTo(Timestamp.create(-24, 42));
    // Negative tests.
    assertThat(Timestamp.create(25, 42)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(24, 43)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-25, 42)).isNotEqualTo(Timestamp.create(-24, 42));
    assertThat(Timestamp.create(-24, 43)).isNotEqualTo(Timestamp.create(-24, 42));
  }
}
