package com.google.instrumentation.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@code TimestampFactory}. */
@RunWith(JUnit4.class)
public class TimestampFactoryTest {
  private static final int NUM_NANOS_PER_MILLI = 1000 * 1000;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  @SuppressWarnings("deprecation")
  public void millisGranularity() {
    for (int i = 0; i < 1000000; i++) {
      Timestamp now = TimestampFactory.now();
      assertThat(now.getSeconds()).isGreaterThan(0L);
      assertThat(now.getNanos() % NUM_NANOS_PER_MILLI).isEqualTo(0);
    }
  }
}
