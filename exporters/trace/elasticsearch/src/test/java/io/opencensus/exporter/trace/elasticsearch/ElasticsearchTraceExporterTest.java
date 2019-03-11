/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.trace.elasticsearch;

import static org.mockito.Mockito.verify;

import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit test for {@link ElasticsearchTraceExporter}. */
@RunWith(JUnit4.class)
public class ElasticsearchTraceExporterTest {

  private static final String REGISTERED_TRACE_EXPORTER_NAME =
      ElasticsearchTraceExporter.class.getName();
  private static final String SAMPLE_APP_NAME = "test-app";
  private static final String ELASTICSEARCH_USERNAME = "username";
  private static final String ELASTICSEARCH_PASSWORD = "passowrd";
  private static final String ELASTICSEARCH_URL = "http://localhost:9200";
  private static final String ELASTICSEARCH_INDEX = "opencensus";
  private static final String ELASTICSEARCH_TYPE = "type";

  @Mock private SpanExporter spanExporter;
  private ElasticsearchTraceConfiguration elasticsearchTraceConfiguration;

  @Before
  public void setUp() {
    elasticsearchTraceConfiguration =
        ElasticsearchTraceConfiguration.builder()
            .setAppName(SAMPLE_APP_NAME)
            .setUserName(ELASTICSEARCH_USERNAME)
            .setPassword(ELASTICSEARCH_PASSWORD)
            .setElasticsearchUrl(ELASTICSEARCH_URL)
            .setElasticsearchIndex(ELASTICSEARCH_INDEX)
            .setElasticsearchType(ELASTICSEARCH_TYPE)
            .build();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testRegisterElasticsearchExporterService() throws Exception {
    Handler handler = new ElasticsearchTraceHandler(elasticsearchTraceConfiguration);
    ElasticsearchTraceExporter.register(spanExporter, handler);
    verify(spanExporter).registerHandler(REGISTERED_TRACE_EXPORTER_NAME, handler);
  }

  @Test
  public void testUnregisterElasticsearchExporterService() throws Exception {
    Handler handler = new ElasticsearchTraceHandler(elasticsearchTraceConfiguration);
    ElasticsearchTraceExporter.register(spanExporter, handler);

    verify(spanExporter).registerHandler(REGISTERED_TRACE_EXPORTER_NAME, handler);

    ElasticsearchTraceExporter.unregister(spanExporter);
    verify(spanExporter).unregisterHandler(REGISTERED_TRACE_EXPORTER_NAME);
  }
}
