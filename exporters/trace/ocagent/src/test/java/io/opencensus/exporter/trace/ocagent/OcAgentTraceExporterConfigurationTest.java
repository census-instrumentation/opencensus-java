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
    assertThat(configuration.getHost()).isNull();
    assertThat(configuration.getPort()).isNull();
    assertThat(configuration.getServiceName()).isNull();
    assertThat(configuration.getUseInsecure()).isNull();
  }

  @Test
  public void setAndGet() {
    OcAgentTraceExporterConfiguration configuration =
        OcAgentTraceExporterConfiguration.builder()
            .setHost("host")
            .setPort(50051)
            .setServiceName("service")
            .setUseInsecure(true)
            .build();
    assertThat(configuration.getHost()).isEqualTo("host");
    assertThat(configuration.getPort()).isEqualTo(50051);
    assertThat(configuration.getServiceName()).isEqualTo("service");
    assertThat(configuration.getUseInsecure()).isTrue();
  }
}
