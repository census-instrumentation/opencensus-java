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

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link ElasticsearchTraceExporter}.
 *
 * @since 0.13
 */
@AutoValue
@Immutable
public abstract class ElasticsearchConfiguration {

  public static Builder builder() {
    return new AutoValue_ElasticsearchConfiguration.Builder();
  }

  /**
   * Retrieve the app name configured.
   *
   * @return A String data type.
   * @since 0.13
   */
  public abstract String getAppName();

  /**
   * Retrieve user name used to access Elasticsearch.
   *
   * @return A String data type.
   * @since 0.13
   */
  @Nullable
  public abstract String getUserName();

  /**
   * Retrieve password used to access Elasticsearch.
   *
   * @return A String data type.
   * @since 0.13
   */
  @Nullable
  public abstract String getPassword();

  /**
   * Retrieve base url for Elasticsearch.
   *
   * @return A String data type.
   * @since 0.13
   */
  public abstract String getElasticsearchUrl();

  /**
   * Retrieve index in Elasticsearch configured for storing trace data.
   *
   * @return A String data index.
   * @since 0.13
   */
  public abstract String getElasticsearchIndex();

  /**
   * Retrieve type in Elasticsearch configured for storing trace data.
   *
   * @return A String data type.
   * @since 0.13
   */
  public abstract String getElasticsearchType();

  /**
   * Builder for {@link ElasticsearchConfiguration}.
   *
   * @since 0.13
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the name of the app used in traces.
     *
     * @param appName the name of app.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setAppName(String appName);

    /**
     * Sets the username of elasticsearch if protected.
     *
     * @param userName of Elasticsearch cluster.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setUserName(String userName);

    /**
     * Sets the password of elasticsearch if protected.
     *
     * @param password of Elasticsearch cluster.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setPassword(String password);

    /**
     * Sets the base URL of Elasticsearch.
     *
     * @param elasticsearchUrl base URL of Elasticsearch.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setElasticsearchUrl(String elasticsearchUrl);

    /**
     * Sets the data index of Elasticsearch.
     *
     * @param elasticsearchIndex the Elasticsearch index.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setElasticsearchIndex(String elasticsearchIndex);

    /**
     * Sets the Elasticsearch type.
     *
     * @param elasticsearchType the Elasticsearch type.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setElasticsearchType(String elasticsearchType);

    /**
     * Builder for {@link ElasticsearchConfiguration}.
     *
     * @return a {@code ElasticsearchConfiguration}.
     * @since 0.13
     */
    public abstract ElasticsearchConfiguration build();
  }
}
