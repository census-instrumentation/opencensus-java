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

package io.opencensus.exporter.trace.elasticsearch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import io.opencensus.exporter.trace.elasticsearch.ElasticsearchTraceExporter.ElasticsearchTraceHandler;
import io.opencensus.trace.export.SpanExporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ElasticsearchTraceExporterTest {

  @Mock private SpanExporter spanExporter;
  private ElasticsearchConfiguration elasticsearchConfiguration;

  @Before
  public void setUp() {
    elasticsearchConfiguration =
        ElasticsearchConfiguration.builder()
            .setAppName("app-name")
            .setUserName("username")
            .setPassword("password")
            .setElasticsearchUrl("http://localhost:9200")
            .setElasticsearchIndex("opencensus")
            .setElasticsearchType("type")
            .build();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerElasticsearchExporterService() throws Exception {

    ElasticsearchTraceExporter.register(
        spanExporter, new ElasticsearchTraceHandler(elasticsearchConfiguration));
    verify(spanExporter).registerHandler(anyString(), any(ElasticsearchTraceHandler.class));

    ElasticsearchTraceExporter.unregister(spanExporter);
    verify(spanExporter).unregisterHandler(anyString());
  }
}
