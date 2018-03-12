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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import io.opencensus.exporter.trace.elasticsearch.exception.InvalidElasticsearchConfigException;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.DatatypeConverter;

/**
 * @autor malike_st
 */
public class ElasticsearchTraceExporter {

  private static final Logger logger = Logger.getLogger(ElasticsearchTraceExporter.class.getName());
  private static final String REGISTER_NAME = ElasticsearchTraceExporter.class.getName();
  private static final Object monitor = new Object();
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ ";
  private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
  private static final String CONTENT_TYPE = "application/json";
  private static final String REQUEST_METHOD = "POST";
  private static final int CONNECTION_TIMEOUT = 6000;


  @GuardedBy("monitor")
  @Nullable
  private static SpanExporter.Handler handler = null;

  public static void createAndRegister(ElasticsearchConfiguration elasticsearchConfiguration)
      throws InvalidElasticsearchConfigException {
    synchronized (monitor) {
      checkState(handler == null, "Elasticsearch exporter already registered.");
      if (elasticsearchConfiguration == null) {
        throw new InvalidElasticsearchConfigException("Elasticsearch configuration not usable");
      }
      checkState(elasticsearchConfiguration.getElasticsearchIndex() != null,
          "Elasticsearch Index not specified");
      checkState(elasticsearchConfiguration.getElasticsearchUrl() != null,
          "Elasticsearch URL not specified");
      registerElasticsearchHandler(new ElasticsearchTraceHandler(elasticsearchConfiguration));
    }

  }

  private static void registerElasticsearchHandler(SpanExporter.Handler elasticsearchHandler) {
    synchronized (monitor) {
      handler = elasticsearchHandler;
      register(Tracing.getExportComponent().getSpanExporter(), handler);
    }
  }


  @VisibleForTesting
  static void register(SpanExporter spanExporter, SpanExporter.Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }


  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null,
          "Can't unregister Elasticsearch Exporter which is not registered. ");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  private static void exportToElasticsearch(SpanData spanData,
      ElasticsearchConfiguration elasticsearchConfiguration) {
    try {
      URL url = new URL(elasticsearchConfiguration.getElasticsearchUrl()+"/"
          +elasticsearchConfiguration.getElasticsearchIndex()+"/"+elasticsearchConfiguration.getElasticsearchIndex());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      if (elasticsearchConfiguration.getUserName() != null
          && !elasticsearchConfiguration.getUserName().isEmpty()) {
        String encoding = DatatypeConverter
            .printBase64Binary((elasticsearchConfiguration.getUserName() + ":" +
                elasticsearchConfiguration.getPassword()).getBytes("UTF-8"));
        connection.setRequestProperty("Authorization", "Basic " + encoding);
      }
      connection.setRequestMethod(REQUEST_METHOD);
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("name", spanData.getName());
      parameters.put("hasRemoteParent", spanData.getHasRemoteParent());
      parameters.put("childSpanCount", spanData.getChildSpanCount());
      parameters.put("status", spanData.getStatus());
      parameters.put("spanId", spanData.getContext().getSpanId().toString());
      parameters.put("traceId", spanData.getContext().getTraceId().toString());
      parameters.put("traceOptions", spanData.getContext().getTraceOptions().toString());
      parameters.put("attributes", spanData.getAttributes().getAttributeMap());
      parameters.put("parentSpanId", spanData.getParentSpanId().toString());
      parameters.put("endTimestamp", SIMPLE_DATE_FORMAT.format(spanData.getEndTimestamp()));
      parameters.put("startTimestamp", SIMPLE_DATE_FORMAT.format(spanData.getEndTimestamp()));
      parameters.put("createdTimestamp", SIMPLE_DATE_FORMAT.format(new Date()));
      connection.setDoOutput(true);
      connection.setConnectTimeout(CONNECTION_TIMEOUT);
      connection.setRequestProperty("Content-Type", CONTENT_TYPE);
      DataOutputStream out = new DataOutputStream(connection.getOutputStream());
      out.writeBytes(new Gson().toJson(parameters));
      out.flush();
      out.close();
    } catch (Exception e) {
      logger.log(Level.FINE, "Error writing to elasticsearch.", e);
    }
  }

  @VisibleForTesting
  public static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }


  static final class ElasticsearchTraceHandler extends SpanExporter.Handler {

    private final ElasticsearchConfiguration elasticsearchConfiguration;

    @VisibleForTesting
    public ElasticsearchTraceHandler(ElasticsearchConfiguration elasticsearchConfiguration) {
      this.elasticsearchConfiguration = elasticsearchConfiguration;
    }

    @Override
    public void export(Collection<SpanData> spanDataList) {
      for (SpanData spanData : spanDataList) {
        exportToElasticsearch(spanData, elasticsearchConfiguration);
      }
    }

  }


}
