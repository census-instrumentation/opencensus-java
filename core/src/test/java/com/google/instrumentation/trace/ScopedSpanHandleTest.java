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

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.instrumentation.common.NonThrowingCloseable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link ScopedSpanHandle}. */
@RunWith(JUnit4.class)
public class ScopedSpanHandleTest {
  @Mock private Span span;

  @Mock private ContextSpanHandler csh;

  @Mock private NonThrowingCloseable ntc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void initAttachesSpan_CloseDetachesAndEndsSpan() {
    when(csh.withSpan(same(span))).thenReturn(ntc);
    try (NonThrowingCloseable ss = new ScopedSpanHandle(span, csh)) {
      // Do nothing.
    }
    verify(ntc).close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }
}
