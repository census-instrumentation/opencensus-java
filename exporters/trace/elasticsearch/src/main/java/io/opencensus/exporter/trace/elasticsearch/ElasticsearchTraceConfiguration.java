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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link ElasticsearchTraceExporter}.
 *
 * @since 0.20.0
 */
@AutoValue
@Immutable
public abstract class ElasticsearchTraceConfiguration {

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.20.0
   */
  public static Builder builder() {
    return new AutoValue_ElasticsearchTraceConfiguration.Builder();
  }

  /**
   * Retrieves the app name configured.
   *
   * @return the name of app to include in traces.
   * @since 0.20.0
   */
  public abstract String getAppName();

  /**
   * Retrieves user name used to access Elasticsearch.
   *
   * @return the username for Elasticsearch.
   * @since 0.20.0
   */
  @Nullable
  public abstract String getUserName();

  /**
   * Retrieves password used to access Elasticsearch.
   *
   * @return the password for Elasticsearch.
   * @since 0.20.0
   */
  @Nullable
  public abstract String getPassword();

  /**
   * Retrieves base url for Elasticsearch.
   *
   * @return the url for Elasticsearch.
   * @since 0.20.0
   */
  public abstract String getElasticsearchUrl();

  /**
   * Retrieves index in Elasticsearch configured for storing trace data.
   *
   * @return the Elasticsearch index where the trace will be saved.
   * @since 0.20.0
   */
  public abstract String getElasticsearchIndex();

  /**
   * Retrieves type in Elasticsearch configured for storing trace data.
   *
   * @return the Elasticsearch type where the trace will be saved.
   * @since 0.20.0
   */
  public abstract String getElasticsearchType();

  /**
   * Builds a {@link ElasticsearchTraceConfiguration}.
   *
   * @since 0.20.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    abstract ElasticsearchTraceConfiguration autoBuild();

    /**
     * Sets the name of the app used in traces.
     *
     * @param appName the name of app to include in traces.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setAppName(String appName);

    /**
     * Sets the username of elasticsearch if protected.
     *
     * @param userName of Elasticsearch cluster.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setUserName(String userName);

    /**
     * Sets the password of elasticsearch if protected.
     *
     * @param password of Elasticsearch cluster.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setPassword(String password);

    /**
     * Sets the base URL of Elasticsearch.
     *
     * @param elasticsearchUrl URL of Elasticsearch.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setElasticsearchUrl(String elasticsearchUrl);

    /**
     * Sets the data index of Elasticsearch.
     *
     * @param elasticsearchIndex the Elasticsearch index.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setElasticsearchIndex(String elasticsearchIndex);

    /**
     * Sets the Elasticsearch type.
     *
     * @param elasticsearchType the Elasticsearch type.
     * @return this.
     * @since 0.20.0
     */
    public abstract Builder setElasticsearchType(String elasticsearchType);

    /**
     * Builder for {@link ElasticsearchTraceConfiguration}.
     *
     * @return a {@code ElasticsearchTraceConfiguration}.
     * @since 0.20.0
     */
    public ElasticsearchTraceConfiguration build() {
      ElasticsearchTraceConfiguration elasticsearchTraceConfiguration = autoBuild();
      Preconditions.checkArgument(
          !Strings.isNullOrEmpty(elasticsearchTraceConfiguration.getAppName()),
          "Invalid App name ");
      Preconditions.checkArgument(
          !Strings.isNullOrEmpty(elasticsearchTraceConfiguration.getElasticsearchIndex()),
          "Invalid Elasticsearch index.");
      Preconditions.checkArgument(
          !Strings.isNullOrEmpty(elasticsearchTraceConfiguration.getElasticsearchIndex()),
          "Invalid Elasticsearch type.");
      return elasticsearchTraceConfiguration;
    }
  }
}
