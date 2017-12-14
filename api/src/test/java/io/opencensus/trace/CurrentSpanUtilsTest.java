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
import java.util.concurrent.Callable;
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

  // TODO(bdrutu): When update to junit 4.13 use assertThrows instead of this.
  private void executeRunnableAndExpectError(Runnable runnable, Throwable error) {
    boolean called = false;
    try {
      CurrentSpanUtils.withSpan(span, true, runnable).run();
    } catch (Throwable e) {
      assertThat(e).isEqualTo(error);
      called = true;
    }
    assertThat(called).isTrue();
  }

  // TODO(bdrutu): When update to junit 4.13 use assertThrows instead of this.
  private void executeCallableAndExpectError(Callable<Object> callable, Throwable error) {
    boolean called = false;
    try {
      CurrentSpanUtils.withSpan(span, true, callable).call();
    } catch (Throwable e) {
      assertThat(e).isEqualTo(error);
      called = true;
    }
    assertThat(called).isTrue();
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
  }

  @Test
  public void withSpan_CloseDetachesAndEndsSpan() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Scope ss = CurrentSpanUtils.withSpan(span, true);
    try {
      assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void withSpanRunnable() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
          }
        };
    CurrentSpanUtils.withSpan(span, false, runnable).run();
    verifyZeroInteractions(span);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanRunnable_EndSpan() {
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
          }
        };
    CurrentSpanUtils.withSpan(span, true, runnable).run();
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanRunnable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw error;
          }
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(Status.UNKNOWN.withDescription("MyError"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanRunnable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw error;
          }
        };
    executeRunnableAndExpectError(runnable, error);
    verify(span).setStatus(Status.UNKNOWN.withDescription("AssertionError"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable() throws Exception {
    final Object ret = new Object();
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            return ret;
          }
        };
    assertThat(CurrentSpanUtils.withSpan(span, false, callable).call()).isEqualTo(ret);
    verifyZeroInteractions(span);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable_EndSpan() throws Exception {
    final Object ret = new Object();
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            return ret;
          }
        };
    assertThat(CurrentSpanUtils.withSpan(span, true, callable).call()).isEqualTo(ret);
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable_WithException() {
    final Exception exception = new Exception("MyException");
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw exception;
          }
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(Status.UNKNOWN.withDescription("MyException"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable_WithExceptionNoMessage() {
    final Exception exception = new Exception();
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw exception;
          }
        };
    executeCallableAndExpectError(callable, exception);
    verify(span).setStatus(Status.UNKNOWN.withDescription("Exception"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable_WithError() {
    final AssertionError error = new AssertionError("MyError");
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw error;
          }
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(Status.UNKNOWN.withDescription("MyError"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }

  @Test
  public void withSpanCallable_WithErrorNoMessage() {
    final AssertionError error = new AssertionError();
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
    Callable<Object> callable =
        new Callable<Object>() {
          @Override
          public Object call() throws Exception {
            // When we run the runnable we will have the span in the current Context.
            assertThat(CurrentSpanUtils.getCurrentSpan()).isSameAs(span);
            throw error;
          }
        };
    executeCallableAndExpectError(callable, error);
    verify(span).setStatus(Status.UNKNOWN.withDescription("AssertionError"));
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(CurrentSpanUtils.getCurrentSpan()).isNull();
  }
}
