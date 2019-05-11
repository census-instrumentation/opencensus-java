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

package io.opencensus.exporter.metrics.ocagent;

import static com.google.common.truth.Truth.assertThat;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.opencensus.common.Duration;
import javax.net.ssl.SSLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcAgentMetricsExporterConfiguration}. */
@RunWith(JUnit4.class)
public class OcAgentMetricsExporterConfigurationTest {

  @Test
  public void defaultConfiguration() {
    OcAgentMetricsExporterConfiguration configuration =
        OcAgentMetricsExporterConfiguration.builder().build();
    assertThat(configuration.getEndPoint())
        .isEqualTo(OcAgentMetricsExporterConfiguration.DEFAULT_END_POINT);
    assertThat(configuration.getServiceName())
        .isEqualTo(OcAgentMetricsExporterConfiguration.DEFAULT_SERVICE_NAME);
    assertThat(configuration.getUseInsecure()).isTrue();
    assertThat(configuration.getSslContext()).isNull();
    assertThat(configuration.getRetryInterval())
        .isEqualTo(OcAgentMetricsExporterConfiguration.DEFAULT_RETRY_INTERVAL);
    assertThat(configuration.getExportInterval())
        .isEqualTo(OcAgentMetricsExporterConfiguration.DEFAULT_EXPORT_INTERVAL);
  }

  @Test
  public void setAndGet() throws SSLException {
    Duration oneMinute = Duration.create(60, 0);
    Duration fiveMinutes = Duration.create(300, 0);
    SslContext sslContext = SslContextBuilder.forClient().build();
    OcAgentMetricsExporterConfiguration configuration =
        OcAgentMetricsExporterConfiguration.builder()
            .setEndPoint("192.168.0.1:50051")
            .setServiceName("service")
            .setUseInsecure(false)
            .setSslContext(sslContext)
            .setRetryInterval(fiveMinutes)
            .setExportInterval(oneMinute)
            .build();
    assertThat(configuration.getEndPoint()).isEqualTo("192.168.0.1:50051");
    assertThat(configuration.getServiceName()).isEqualTo("service");
    assertThat(configuration.getUseInsecure()).isFalse();
    assertThat(configuration.getSslContext()).isEqualTo(sslContext);
    assertThat(configuration.getRetryInterval()).isEqualTo(fiveMinutes);
    assertThat(configuration.getExportInterval()).isEqualTo(oneMinute);
  }
}
