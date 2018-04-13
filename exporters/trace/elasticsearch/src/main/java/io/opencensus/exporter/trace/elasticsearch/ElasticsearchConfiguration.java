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

public class ElasticsearchConfiguration {


  private final String appName;
  private final String userName;
  private final String password;
  private final String elasticsearchUrl;
  private final String elasticsearchIndex;
  private final String elasticsearchType;

  /**
   * Configurations required by {@code ElasticsearchTraceExporter} to connect to Elasticsearch
   *
   * @param appName the name of the service used to trace.
   * @param userName the username required to connect to Elasticsearch. Leave as null if it's not
   * required
   * @param password the password required to connect to Elasticsearch. Leave as null if it's not
   * required
   * @param elasticsearchUrl the base URL for Elasticsearch. eg: "http://localhost:9200"
   * @param elasticsearchIndex the index to store trace data in Elasticsearch. eg: "trace_index"
   * @param elasticsearchType the type to store trace data in Elasticsearch. eg: "trace_type"
   * @since 0.13
   */
  public ElasticsearchConfiguration(String appName, String userName,
      String password, String elasticsearchUrl, String elasticsearchIndex,
      String elasticsearchType) {
    this.appName = appName;
    this.userName = userName;
    this.password = password;
    this.elasticsearchUrl = elasticsearchUrl;
    this.elasticsearchIndex = elasticsearchIndex;
    this.elasticsearchType = elasticsearchType;
  }

  /**
   * Retrieve the app name configured.
   *
   * @return A String data type.
   */
  public String getAppName() {
    return appName;
  }

  /**
   * Retrieve user name used to access Elasticsearch.
   *
   * @return A String data type.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Retrieve password used to access Elasticsearch.
   *
   * @return A String data type.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Retrieve base url for Elasticsearch.
   *
   * @return A String data type.
   */
  public String getElasticsearchUrl() {
    return elasticsearchUrl;
  }

  /**
   * Retrieve index in Elasticsearch configured for storing trace data.
   *
   * @return A String data index.
   */
  public String getElasticsearchIndex() {
    return elasticsearchIndex;
  }

  /**
   * Retrieve type in Elasticsearch configured for storing trace data.
   *
   * @return A String data type.
   */
  public String getElasticsearchType() {
    return elasticsearchType;
  }
}
