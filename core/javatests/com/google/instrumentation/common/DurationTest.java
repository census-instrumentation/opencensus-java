package com.google.instrumentation.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Duration}. */
@RunWith(JUnit4.class)
public class DurationTest {
  @Test
  public void testDurationFromMillis() {
    assertThat(Duration.fromMillis(0).getSeconds()).isEqualTo(0);
    assertThat(Duration.fromMillis(0).getNanos()).isEqualTo(0);
    assertThat(Duration.fromMillis(987).getSeconds()).isEqualTo(0);
    assertThat(Duration.fromMillis(987).getNanos()).isEqualTo(987000000);
    assertThat(Duration.fromMillis(3456).getSeconds()).isEqualTo(3);
    assertThat(Duration.fromMillis(3456).getNanos()).isEqualTo(456000000);
  }

  @Test
  public void testDurationFromMillisNegative() {
    assertThat(Duration.fromMillis(-1).getSeconds()).isEqualTo(0);
    assertThat(Duration.fromMillis(-1).getNanos()).isEqualTo(-1000000);
    assertThat(Duration.fromMillis(-999).getSeconds()).isEqualTo(0);
    assertThat(Duration.fromMillis(-999).getNanos()).isEqualTo(-999000000);
    assertThat(Duration.fromMillis(-1000).getSeconds()).isEqualTo(-1);
    assertThat(Duration.fromMillis(-1000).getNanos()).isEqualTo(0);
    assertThat(Duration.fromMillis(-3456).getSeconds()).isEqualTo(-3);
    assertThat(Duration.fromMillis(-3456).getNanos()).isEqualTo(-456000000);
  }

  @Test
  public void testDurationInvalidInput() {
    assertThat(Duration.create(-315576000001L, 0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(315576000001L, 0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(0, 1000000000)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(0, -1000000000)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(-1, 1)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(1, -1)).isEqualTo(Duration.create(0, 0));
  }

  @Test
  public void testDurationEqual() {
    assertThat(Duration.fromMillis(0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.fromMillis(987)).isEqualTo(Duration.create(0, 987000000));
    assertThat(Duration.fromMillis(3456)).isEqualTo(Duration.create(3, 456000000));
    assertThat(Duration.fromMillis(-987)).isEqualTo(Duration.create(0, -987000000));
    assertThat(Duration.fromMillis(-3456)).isEqualTo(Duration.create(-3, -456000000));
  }
}
