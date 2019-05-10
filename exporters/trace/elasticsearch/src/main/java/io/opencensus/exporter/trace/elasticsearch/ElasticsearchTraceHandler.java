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

import com.google.common.io.BaseEncoding;
import io.opencensus.exporter.trace.util.TimeLimitedHandler;
import io.opencensus.trace.export.SpanData;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

final class ElasticsearchTraceHandler extends TimeLimitedHandler {

  private final ElasticsearchTraceConfiguration elasticsearchTraceConfiguration;
  private final String appName;
  private final URL indexUrl;
  private static final String CONTENT_TYPE = "application/json";
  private static final String REQUEST_METHOD = "POST";
  private static final int CONNECTION_TIMEOUT_MILLISECONDS = 6000;
  private static final String EXPORT_SPAN_NAME = "ExportElasticsearchTraces";

  ElasticsearchTraceHandler(ElasticsearchTraceConfiguration elasticsearchTraceConfiguration)
      throws MalformedURLException {
    super(elasticsearchTraceConfiguration.getDeadline(), EXPORT_SPAN_NAME);
    this.elasticsearchTraceConfiguration = elasticsearchTraceConfiguration;
    StringBuilder sb = new StringBuilder();
    sb.append(elasticsearchTraceConfiguration.getElasticsearchUrl()).append("/");
    sb.append(elasticsearchTraceConfiguration.getElasticsearchIndex()).append("/");
    sb.append(elasticsearchTraceConfiguration.getElasticsearchType()).append("/");
    indexUrl = new URL(sb.toString());
    appName = elasticsearchTraceConfiguration.getAppName();
  }

  /**
   * Handles exporting of traces in {@code ElasticsearchTraceExporter}.
   *
   * @param spanDataList Collection of {@code SpanData} to be exported.
   */
  @Override
  public void timeLimitedExport(Collection<SpanData> spanDataList) throws Exception {
    List<String> jsonList = JsonConversionUtils.convertToJson(appName, spanDataList);
    if (jsonList.isEmpty()) {
      return;
    }
    for (String json : jsonList) {

      OutputStream outputStream = null;
      InputStream inputStream = null;

      try {
        HttpURLConnection connection = (HttpURLConnection) indexUrl.openConnection();
        if (elasticsearchTraceConfiguration.getUserName() != null) {
          String parameters =
              BaseEncoding.base64()
                  .encode(
                      (elasticsearchTraceConfiguration.getUserName()
                              + ":"
                              + elasticsearchTraceConfiguration.getPassword())
                          .getBytes("UTF-8"));
          connection.setRequestProperty("Authorization", "Basic " + parameters);
        }
        connection.setRequestMethod(REQUEST_METHOD);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        outputStream = connection.getOutputStream();
        outputStream.write(json.getBytes(Charset.defaultCharset()));
        outputStream.flush();
        inputStream = connection.getInputStream();
        if (connection.getResponseCode() != 200) {
          throw new Exception("Response " + connection.getResponseCode());
        }
      } finally {
        closeStream(inputStream);
        closeStream(outputStream);
      }
    }
  }

  // Closes an input or output stream and ignores potential IOException.
  private static void closeStream(@Nullable Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }
}
