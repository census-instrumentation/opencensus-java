/*
 * Copyright 2017, Google Inc.
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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;

import io.opencensus.common.Scope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link ScopedSpanHandle}. */
@RunWith(JUnit4.class)
public class ScopedSpanHandleTest {
  private static final Tracer tracer = Tracer.getNoopTracer();
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void initAttachesSpan_CloseDetachesAndEndsSpan() {
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    Scope ss = new ScopedSpanHandle(span);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }
}
