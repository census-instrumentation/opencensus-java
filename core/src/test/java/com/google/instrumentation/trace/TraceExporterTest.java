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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.google.instrumentation.trace.TraceExporter.LoggingServiceHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TraceExporter}. */
@RunWith(JUnit4.class)
public class TraceExporterTest {
  @Mock private TraceExporter traceExporter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerUnregisterLoggingService() {
    LoggingServiceHandler.registerService(traceExporter);
    verify(traceExporter)
        .registerServiceHandler(
            eq("com.google.instrumentation.trace.LoggingServiceHandler"),
            any(LoggingServiceHandler.class));
    LoggingServiceHandler.unregisterService(traceExporter);
    verify(traceExporter)
        .unregisterServiceHandler(eq("com.google.instrumentation.trace.LoggingServiceHandler"));
  }
}
