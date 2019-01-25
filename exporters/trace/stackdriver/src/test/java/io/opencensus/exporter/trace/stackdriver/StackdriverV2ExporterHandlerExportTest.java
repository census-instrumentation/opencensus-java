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

package io.opencensus.exporter.trace.stackdriver;

import static org.mockito.Mockito.when;

import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.stub.TraceServiceStub;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.export.SpanData;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for exporting in {@link StackdriverV2ExporterHandler}. */
@RunWith(JUnit4.class)
public final class StackdriverV2ExporterHandlerExportTest {
  private static final String PROJECT_ID = "PROJECT_ID";
  // mock the service stub to provide a fake trace service.
  @Mock private TraceServiceStub traceServiceStub;
  private TraceServiceClient traceServiceClient;
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private StackdriverV2ExporterHandler handler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    // TODO(@Hailong): TraceServiceClient.create(TraceServiceStub) is a beta API and might change
    // in the future.
    traceServiceClient = TraceServiceClient.create(traceServiceStub);
    handler =
        StackdriverV2ExporterHandler.createWithStub(
            PROJECT_ID, traceServiceClient, Collections.<String, AttributeValue>emptyMap());
  }

  @Test
  public void export() {
    when(traceServiceStub.batchWriteSpansCallable())
        .thenThrow(new RuntimeException("TraceServiceStub called"));
    Collection<SpanData> spanDataList = Collections.<SpanData>emptyList();
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("TraceServiceStub called");
    handler.export(spanDataList);
  }
}
