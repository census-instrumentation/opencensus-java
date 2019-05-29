/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent.bootstrap;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import java.io.Closeable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link TraceTrampoline}. */
@RunWith(MockitoJUnitRunner.class)
public class TraceTrampolineTest {

  private static final TraceStrategy mockTraceStrategy = mock(TraceStrategy.class);

  static {
    TraceTrampoline.setTraceStrategy(mockTraceStrategy);
  }

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void setTraceStrategy_already_initialized() {
    exception.expect(IllegalStateException.class);

    TraceTrampoline.setTraceStrategy(mockTraceStrategy);
  }

  @Test
  @SuppressWarnings("MustBeClosedChecker")
  public void startScopedSpan() {
    Closeable mockCloseable = mock(Closeable.class);
    Mockito.when(mockTraceStrategy.startScopedSpan("test")).thenReturn(mockCloseable);

    Closeable closeable = TraceTrampoline.startScopedSpan("test");

    Mockito.verify(mockTraceStrategy).startScopedSpan("test");
    assertThat(closeable).isSameAs(mockCloseable);
  }
}
