package com.google.instrumentation.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimestampFactory}. */
@RunWith(JUnit4.class)
public class TimestampFactoryTest {
  private static final int NUM_NANOS_PER_MILLI = 1000 * 1000;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void millisGranularity() {
    for (int i = 0; i < 1000000; i++) {
      Timestamp now = TimestampFactory.now();
      assertThat(now.getSeconds()).isGreaterThan(0L);
      assertThat(now.getNanos() % NUM_NANOS_PER_MILLI).isEqualTo(0);
    }
  }

  @Test
  public void loadProtoPropagationHandler_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    TimestampFactory.loadTimestampFactoryHandler(new ClassLoader() {
      @Override
      public Class<?> loadClass(String name) {
        throw toThrow;
      }
    });
  }

  @Test
  public void loadProtoPropagationHandler_IgnoresMissingClasses() {
    assertThat(TimestampFactory
                   .loadTimestampFactoryHandler(new ClassLoader() {
                     @Override
                     public Class<?> loadClass(String name) throws ClassNotFoundException {
                       throw new ClassNotFoundException();
                     }
                   })
                   .getClass()
                   .getName())
        .isEqualTo("com.google.instrumentation.common.TimestampFactory$DefaultHandler");
  }
}
