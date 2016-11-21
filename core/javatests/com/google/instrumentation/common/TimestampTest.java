package com.google.instrumentation.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Timestamp}. */
@RunWith(JUnit4.class)
public class TimestampTest {
  @Test
  public void testTimestampFromMillis() {
    assertThat(Timestamp.fromMillis(0).getSeconds()).isEqualTo(0);
    assertThat(Timestamp.fromMillis(0).getNanos()).isEqualTo(0);
    assertThat(Timestamp.fromMillis(987).getSeconds()).isEqualTo(0);
    assertThat(Timestamp.fromMillis(987).getNanos()).isEqualTo(987000000);
    assertThat(Timestamp.fromMillis(3456).getSeconds()).isEqualTo(3);
    assertThat(Timestamp.fromMillis(3456).getNanos()).isEqualTo(456000000);
  }

  @Test
  public void testTimestampFromMillisNegative() {
    assertThat(Timestamp.fromMillis(-1).getSeconds()).isEqualTo(0);
    assertThat(Timestamp.fromMillis(-1).getNanos()).isEqualTo(-1000000);
    assertThat(Timestamp.fromMillis(-999).getSeconds()).isEqualTo(0);
    assertThat(Timestamp.fromMillis(-999).getNanos()).isEqualTo(-999000000);
    assertThat(Timestamp.fromMillis(-3456).getSeconds()).isEqualTo(-3);
    assertThat(Timestamp.fromMillis(-3456).getNanos()).isEqualTo(456000000);
  }

  @Test
  public void testTimestampEqual() {
    assertThat(Timestamp.fromMillis(0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.fromMillis(987)).isEqualTo(Timestamp.create(0, 987000000));
    assertThat(Timestamp.fromMillis(3456)).isEqualTo(Timestamp.create(3, 456000000));
  }
}
