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

package io.opencensus.exporter.trace.jaeger;

import static com.google.common.truth.Truth.assertThat;

import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.opencensus.common.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Unit tests for {@link JaegerExporterConfiguration}. */
@RunWith(JUnit4.class)
public class JaegerExporterConfigurationTest {

  private static final String SERVICE = "service";
  private static final String END_POINT = "endpoint";
  private static final Duration ONE_MIN = Duration.create(60, 0);
  private static final Duration NEG_ONE_MIN = Duration.create(-60, 0);

  @Mock private static final ThriftSender mockSender = Mockito.mock(ThriftSender.class);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void updateConfigs() {
    JaegerExporterConfiguration configuration =
        JaegerExporterConfiguration.builder()
            .setServiceName(SERVICE)
            .setDeadline(ONE_MIN)
            .setThriftSender(mockSender)
            .setThriftEndpoint(END_POINT)
            .build();
    assertThat(configuration.getServiceName()).isEqualTo(SERVICE);
    assertThat(configuration.getDeadline()).isEqualTo(ONE_MIN);
    assertThat(configuration.getThriftEndpoint()).isEqualTo(END_POINT);
    assertThat(configuration.getThriftSender()).isEqualTo(mockSender);
  }

  @Test
  public void needEitherThriftEndpointOrSender() {
    JaegerExporterConfiguration.Builder builder =
        JaegerExporterConfiguration.builder().setServiceName(SERVICE);
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }

  @Test
  public void disallowZeroDuration() {
    JaegerExporterConfiguration.Builder builder =
        JaegerExporterConfiguration.builder().setServiceName(SERVICE);
    builder.setDeadline(JaegerExporterConfiguration.Builder.ZERO);
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }

  @Test
  public void disallowNegativeDuration() {
    JaegerExporterConfiguration.Builder builder =
        JaegerExporterConfiguration.builder().setServiceName(SERVICE);
    builder.setDeadline(NEG_ONE_MIN);
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }
}
