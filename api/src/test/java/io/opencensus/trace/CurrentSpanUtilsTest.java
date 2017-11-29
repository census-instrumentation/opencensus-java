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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.trace.unsafe.ContextUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CurrentSpanUtils}. */
@RunWith(JUnit4.class)
public class CurrentSpanUtilsTest {
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getCurrentSpan_WhenNoContext() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void getCurrentSpan() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Context origContext = Context.current().withValue(ContextUtils.CONTEXT_SPAN_KEY, span).attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpan_CloseDetaches() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Scope ws = CurrentSpanUtils.withSpan(span, false);
    try {
      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      ws.close();
    }
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    verifyZeroInteractions(span);
    ;
  }

  @Test
  public void withSpan_CloseDetachesAndEndsSpan() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Scope ss = CurrentSpanUtils.withSpan(span, true);
    ;
    try {
      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void propagationViaRunnable() {
    Runnable runnable = null;
    Scope ws = CurrentSpanUtils.withSpan(span, false);
    try {
      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
                    }
                  });
    } finally {
      ws.close();
    }
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNotSameAs(span);
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
  }
}
