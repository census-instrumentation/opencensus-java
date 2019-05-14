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

package io.opencensus.exporter.trace.ocagent;

import static com.google.common.truth.Truth.assertThat;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.opencensus.common.Duration;
import javax.net.ssl.SSLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcAgentTraceExporterConfiguration}. */
@RunWith(JUnit4.class)
public class OcAgentTraceExporterConfigurationTest {

  @Test
  public void defaultConfiguration() {
    OcAgentTraceExporterConfiguration configuration =
        OcAgentTraceExporterConfiguration.builder().build();
    assertThat(configuration.getEndPoint())
        .isEqualTo(OcAgentTraceExporterConfiguration.DEFAULT_END_POINT);
    assertThat(configuration.getServiceName())
        .isEqualTo(OcAgentTraceExporterConfiguration.DEFAULT_SERVICE_NAME);
    assertThat(configuration.getUseInsecure()).isTrue();
    assertThat(configuration.getSslContext()).isNull();
    assertThat(configuration.getRetryInterval())
        .isEqualTo(OcAgentTraceExporterConfiguration.DEFAULT_RETRY_INTERVAL);
    assertThat(configuration.getEnableConfig()).isTrue();
    assertThat(configuration.getDeadline())
        .isEqualTo(OcAgentTraceExporterConfiguration.DEFAULT_DEADLINE);
  }

  @Test
  public void setAndGet() throws SSLException {
    Duration oneMinute = Duration.create(60, 0);
    SslContext sslContext = SslContextBuilder.forClient().build();
    OcAgentTraceExporterConfiguration configuration =
        OcAgentTraceExporterConfiguration.builder()
            .setEndPoint("192.168.0.1:50051")
            .setServiceName("service")
            .setUseInsecure(false)
            .setSslContext(sslContext)
            .setRetryInterval(oneMinute)
            .setEnableConfig(false)
            .setDeadline(oneMinute)
            .build();
    assertThat(configuration.getEndPoint()).isEqualTo("192.168.0.1:50051");
    assertThat(configuration.getServiceName()).isEqualTo("service");
    assertThat(configuration.getUseInsecure()).isFalse();
    assertThat(configuration.getSslContext()).isEqualTo(sslContext);
    assertThat(configuration.getRetryInterval()).isEqualTo(oneMinute);
    assertThat(configuration.getEnableConfig()).isFalse();
    assertThat(configuration.getDeadline()).isEqualTo(oneMinute);
  }
}
