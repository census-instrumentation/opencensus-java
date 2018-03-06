package io.opencensus.exporter.trace.elasticsearch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import io.opencensus.exporter.trace.elasticsearch.ElasticsearchTraceExporter.ElasticsearchTraceHandler;
import io.opencensus.exporter.trace.elasticsearch.exception.InvalidElasticsearchConfigException;
import io.opencensus.trace.export.SpanExporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @autor malike_st
 */
@RunWith(JUnit4.class)
public class ElasticsearchTraceExporterTest {

  @Mock
  private SpanExporter spanExporter;
  private ElasticsearchConfiguration elasticsearchConfiguration;


  @Before
  public void setUp() {
    elasticsearchConfiguration = new ElasticsearchConfiguration(null, null, "http://url.com", "test");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerElasticsearchExporterService()
      throws InvalidElasticsearchConfigException {

    ElasticsearchTraceExporter.register(spanExporter,
        new ElasticsearchTraceHandler(elasticsearchConfiguration));
    verify(spanExporter)
        .registerHandler(
            anyString(),
            any(ElasticsearchTraceHandler.class));


    ElasticsearchTraceExporter.unregister(spanExporter);
    verify(spanExporter)
        .unregisterHandler(anyString());
  }


}
