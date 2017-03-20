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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.common.NonThrowingCloseable;
import io.grpc.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link ContextUtils}. */
public class ContextUtilsTest {
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getCurrentSpan_WhenNoContext() {
    assertThat(ContextUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void getCurrentSpan() {
    assertThat(ContextUtils.getCurrentSpan()).isNull();
    Context origContext = Context.current().withValue(ContextUtils.CONTEXT_SPAN_KEY, span).attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(ContextUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(ContextUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpan() {
    assertThat(ContextUtils.getCurrentSpan()).isNull();
    NonThrowingCloseable ws = ContextUtils.withSpan(span);
    try {
      assertThat(ContextUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      ws.close();
    }
    assertThat(ContextUtils.getCurrentSpan()).isNull();
    ;
  }

  @Test
  public void propagationViaRunnable() {
    Runnable runnable = null;
    NonThrowingCloseable ws = ContextUtils.withSpan(span);
    try {
      assertThat(ContextUtils.getCurrentSpan()).isSameAs(span);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(ContextUtils.getCurrentSpan()).isSameAs(span);
                    }
                  });
    } finally {
      ws.close();
    }
    assertThat(ContextUtils.getCurrentSpan()).isNotSameAs(span);
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
  }
}
