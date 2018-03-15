/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.http;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link HttpSpanCustomizer}. */
@RunWith(JUnit4.class)
public class HttpSpanCustomizerTest {

  @Mock private SpanBuilder spanBuilder;
  @Mock private HttpExtractor<Object, Object> extractor;
  @Mock private Span span;

  private final Object request = new Object();
  private final Object response = new Object();
  private final Exception error = new Exception("test");
  private final HttpSpanCustomizer<Object, Object> customizer =
      new HttpSpanCustomizer<Object, Object>();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaultImplementationsDoNotCrash() {
    customizer.getSpanName(request, extractor);
    customizer.customizeSpanBuilder(request, spanBuilder, extractor);
    customizer.customizeSpanStart(request, span, extractor);
    customizer.customizeSpanEnd(response, error, span, extractor);
  }

  @Test
  public void defaultSpanEndShouldSetStatus() {
    customizer.customizeSpanEnd(response, error, span, extractor);
    verify(span).setStatus(any(Status.class));
  }
}
